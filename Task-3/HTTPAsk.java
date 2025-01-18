import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import tcpclient.TCPClient; // Importing the external class that is currently located in the "tcpclient" map directory.


public class HTTPAsk {

    public static void main(String[] args) {

        if (args.length != 1){ // Ensuring that the port number is provided
            System.err.println("Port number needs to be provided");
            System.exit(1); // The program exits immediately after printing the error message.
        }
	
	// Parse port number from command line argument
        int port = Integer.parseInt(args[0]); // Parse port number from command line

        if(port <= 1024) { // Checking if the port number is valid
            System.err.println("Error: Port number must be greater than 1024"); 
            System.exit(1); 
        }

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println(" The server is established in port: " + port);

            while (true) { // Accepting incoming client connection
                Socket client = server.accept(); // Accept incoming client connection
                System.out.println(" The server has accepted the client connection in port: " + port);

                clientRequest(client); // Handling the client request            
            }
        } catch (IOException e){
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String extractURLFromRequest(String request) { // Method that extracts the parameter values from URL.                                                                 
    String[] lines = request.split("\n");                   // It extracts the URL from the first line of the request header. Here's how it works: 
    String requestLine = lines[0];                                // In the array called "lines", we're splitting the request string into an array of lines using the newline character
    return requestLine.split("\\s+")[1];                    // Then it splits the first line usin whitespace as the delimeter and returns the second element, which is usually the URL
    }

private static Map<String, String> extractParameters(String url) { // We're using the Map function to store the parameters extracted from the URL, for a efficient retrieval, but especially for paring the matching values between the key and it's corresponding value.
    Map<String, String> parameters = new HashMap<>();       // After storing the pairs in the map, we're using it to process and fulfill the client request.
    try {
        String queries = url.substring(url.indexOf('?') + 1); // Inside the try-catch block, we're ensuring that the parsing is error free.
        String[] keyValuePairs = queries.split("&");       // The extracted query string is the part of the URL that follows the question mark, and contains key-value pairs separated by the '&'.
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : ""; // Handle case where no value is provided
            parameters.put(key, value);
        }
    } catch (Exception e) {
        e.printStackTrace(); // Handle any exceptions, such as malformed URLs. 
    }
    return parameters; // The parameters in the map contains the extracted key-value pairs, which in turn is returned from the method.
}

   private static void clientRequest(Socket client) { // The privately declared method that can only be accessed in the scope of the class. It handles the client request recieved through the provided Socket 'client' object.
    try ( // Using the byte streams I/O for explicit encoding / decoding: 
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream())); // The two streams are initialized, where the BufferedReader in reads the text from the input stream of the Socket, decoding it using the default character encoding + buffering characters. 
        PrintWriter out = new PrintWriter(client.getOutputStream(), true); // The PrintWriter out writes formatted representations of objects to the output stream of the Socket object in which it flushes automatically after each call to println();
    ) {
        StringBuilder buildedRequest = new StringBuilder();
        String requestLine;

        while ((requestLine = in.readLine()) != null && !requestLine.isEmpty()) { // Reading every row from the HTTP request from client.
            buildedRequest.append(requestLine).append("\r\n"); // Add each line to the StringBuilder. Each line is appended in the buildedRequest with a carriage return until it encounters an empty line. 
        }

        // Processing the HTTP request
        String requestHttp = buildedRequest.toString(); // processing the HTTP request that we've obtained in the buildedRequest. 
        if (!requestHttp.startsWith("GET") || !requestHttp.contains("HTTP/1.1")) { // The validated request starts with 'GET' and contains HTTP/1.1, or else it is considered an invalid request.
            System.out.println("Invalid HTTP Request: " + requestHttp);
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("400 Bad Request: Invalid HTTP Method or Version");
            client.close(); // closing the client socket to terminate connection, where the method is terminated early using the: 
            return; // return statement (terminates the method execution)
        }

        // Extracting URL and parameters from the request
        String url = extractURLFromRequest(requestHttp);
        
        if (!url.startsWith("/ask")) { // Check if the requested URL starts with "/ask"
            System.out.println("Invalid HTTP Request: " + requestHttp);
            System.out.println("Invalid Resource: " + url);
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("404 Not Found: Resource Not Found");
            client.close();
            return;
        }

        Map<String, String> parameters = extractParameters(url);

        // Processing parameters
        String hostname = parameters.getOrDefault("hostname", "");
        int portToClient = Integer.parseInt(parameters.getOrDefault("port", "0"));
        String stringToSend = parameters.getOrDefault("string", "");

        // Sending the HTTP response. This block ensures that both 'hostname' and 'port' parameters are present in the request. If either of them is missing, the status code is set to 400 Bad Request
        if (hostname.isEmpty() || portToClient == 0) {        // We have a conditional check to see if either the 'hostname' is empty or the portClient is 0, indicating that one or both of these 
                                                            // - required are missing. As the client has connected to a port number without providing parameters, this block will be executed.

            System.out.println("Missing required parameters in request: " + requestHttp); 
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("400 Bad Request: Missing required parameters"); 
            client.close();
            return;
        }

        try { // this section is responsible for processing the client's request and generating an appropriate HTTP 
	     //response. It is wrapped in a try - catch block to handle exceptions that may occur during the processing of 
	     //the request or the generation of the response. The error is caught and handled appropriately: 

            System.out.println("Processing request: " + requestHttp);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println();
		
	    // Sends the request to remote server using TCPClient class. We're doing so by creating an instance of the TCP 		
	   // class, which happens to be responsible for establishing a TCP connection to a remote server and sending a 
	   // request. 

            TCPClient tcpInstance = new TCPClient(false, null, null);
            byte[] response = tcpInstance.askServer(hostname, portToClient, stringToSend.getBytes());
            out.println(new String(response, StandardCharsets.UTF_8)); // using the default UTF-8 

        } catch (Exception e) { // This block is executed if there's an exception during the processing of the client request or the generation of the HTTp response.
            System.out.println("Internal Server Error: " + e.getMessage()); // The status code 500 is sent back to the client 
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/plain");
            out.println();                                                  // Empty line is added to indicate the end of the HTTP header. 
            out.println("500 Internal Server Error: " + e.getMessage());    // Response body along with the error message, sent to the client.
        }

        // Close the client socket
        client.close();
    } catch (IOException e) {
        System.err.println("Error handling client request: " + e.getMessage());
     }
  }
}