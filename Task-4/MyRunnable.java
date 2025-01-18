import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import tcpclient.TCPClient;

public class MyRunnable implements Runnable { // Step 1: Define a class that implements the Runnable interface. (Let's call this class MyRunnable.)

    private Socket clientSocket; // The privately declared variable intends to be accessable solely within the MyRunnable class. 

    public MyRunnable(Socket clientSocket) { // Constructor to initialize the clientSocket ( with the provided socket)
        this.clientSocket = clientSocket; // The constructor assigns the value of the clientSocket parameter to the instance variable (with the same name). 
    } 

    @Override // Step 5: Implement the run method where the thread will do its work
    public void run() { 
        try (
        	// here we're setting up the input and output streams for communication with the client. 
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Sets up the required input and output streams for communication with the client.
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            StringBuilder requestBuilder = new StringBuilder(); // Here we are reading the request, line by line and then append it to the StringBuilder.
            String line;
            // Below is the while loop where we are reading the HTTP request line by line and appending it to a String Builder.
            // Read the request line by line
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString(); // Convert the request to a string
            System.out.println("HTTP Request:\n" + request); // Print out the HTTP request
            // We're checking if the request follows the HTTP protocol
            // Task 3: Validate the request: 
            if (!request.startsWith("GET") || !request.contains("HTTP/1.1")) { // We're checking if the request follows the HTTP protocol.
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("400 Bad Request: Invalid HTTP Method or Version");
                clientSocket.close(); // Closing the client socket
                return;
            }

            String url = extractURLFromRequest(request); // Extracting the URl from the request

            if (!url.startsWith("/ask")) { // Check if the request recourse is "/ask"
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("404 Not Found: Resource Not Found");
                clientSocket.close(); // closing and returning
                return;
            }

            Map<String, String> parameters = extractParameters(url); // Extracing the parameters from the URL.

            String hostname = parameters.getOrDefault("hostname", ""); // We are retrieving the hostname, port, and string to send from parameters. 
            int port = Integer.parseInt(parameters.getOrDefault("port", "0"));
            String string_To_Send = parameters.getOrDefault("string", "");

            if (hostname.isEmpty() || port == 0) {
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("400 Bad Request: Missing required parameters");
                clientSocket.close();
                return;
            }

            try { // this section is responsible for processing the client's request and generating an appropriate HTTP 
	     		//response. It is wrapped in a try - catch block to handle exceptions that may occur during the processing of 
	     		//the request or the generation of the response. The error is caught and handled appropriately: 

                out.println("HTTP/1.1 200 OK"); // Task 3: Send HTTP 200 OK response
                out.println("Content-Type: text/plain");
                out.println();

                // Sends the request to remote server using TCPClient class. We're doing so by creating an instance of the TCP 		
	  			// class, which happens to be responsible for establishing a TCP connection to a remote server and sending a 
	  		 	// request. 
                TCPClient client = new TCPClient(false, null, null);
                byte[] response = client.askServer(hostname, port, string_To_Send.getBytes());
                out.println(new String(response, StandardCharsets.UTF_8));

            } catch (Exception e) {
                out.println("HTTP/1.1 500 Internal Server Error");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("500 Internal Server Error: " + e.getMessage());
            }
        } catch (Exception e) { 
            System.err.println("Error handling client request: " + e.getMessage()); // Task 3: Handling exceptions while processing client request
        } finally {
            try {
                clientSocket.close(); // Close the client socket
            } catch (Exception e) { 
                System.err.println("Error closing client socket: " + e.getMessage()); // Task 3: Handling exceptions while closing the client socket. 
            }
        }
    }

    private String extractURLFromRequest(String request) { // Helper method from Task 3 to extract the parameters from URL
        String[] lines = request.split("\n");
        String requestLine = lines[0];
        return requestLine.split("\\s+")[1];
    }

    private Map<String, String> extractParameters(String url) {
        Map<String, String> parameters = new HashMap<>();
        try {
            String queries = url.substring(url.indexOf('?') + 1);
            String[] keyValuePairs = queries.split("&");
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=");
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";
                parameters.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }
}
