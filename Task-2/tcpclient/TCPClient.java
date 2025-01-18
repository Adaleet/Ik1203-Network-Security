package tcpclient;

import java.io.*;
import java.net.*;

public class TCPClient {

    private boolean shutdown; // Indicates whether the client should shut down the connection in the outgoing direction
    private Integer timeout; // Maximum time the client should wait for data before returning (can be null)
    private Integer limit; // Maximum amount of data the client should receive before returning (can be null)

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        // We're refering to the instance variables that belongs to the constructor, and passing down the values from the parameters to these variables by assigning them.
        this.shutdown = shutdown; 
        this.timeout = timeout;
        this.limit = limit;
    }

    // Method to query the server and receive data, with the given hostname, port, and data to send
    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
        Socket socket = new Socket(hostname, port);; 
        InputStream in = socket.getInputStream(); // Create an input stream to receive data from the server
        OutputStream out = socket.getOutputStream(); // Create an output stream to send data to the server

        if (timeout != null) {
            socket.setSoTimeout(timeout);
        }

        if (toServerBytes != null) {
            out.write(toServerBytes);
            if (shutdown) { // If the shutdown indicator is set, shut down the outgoing connection after sending data
                System.out.println("TEST COMPLETE! PROGRAM IS SHUTDOWN!!!");
                socket.shutdownOutput(); // Socket class that is meant to close the output stream of the socket.
            }
        }

        byte[] buffer = new byte[1024]; // Initializing a buffer with the purpose to read the data from the inputStream at "InputStream in"
        ByteArrayOutputStream responseData = new ByteArrayOutputStream(); // Create a buffer to store the server's response
        int bytes; // Counting the number of bytes that is being read.

        try { 
            // Loop to read data from the server until one of the return conditions is met
            while ((bytes = in.read(buffer)) != -1) {
                responseData.write(buffer, 0, bytes); // Add the read data to the buffer

                // Check if the data limit is reached and break the loop if so, break out of the loop:
                if (limit != null && responseData.size() >= limit) {
                    System.out.println("SHUTDOWN DUE TO LIMITATION!!!!!");
                    break;
                }
            }
        } catch (SocketTimeoutException e) { // Time exceeding exception included, if the timeout is exceeded.
            System.out.println("SHUTOWN DUE TO TIMEOUT!!!!!!");
        }
        socket.close();
        return responseData.toByteArray();
    }
}