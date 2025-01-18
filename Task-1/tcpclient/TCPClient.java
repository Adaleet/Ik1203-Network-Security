package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {

    public TCPClient() { // Empty constructor, the instructor has no parameters
        
    }
    
    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {
        Socket socket = null; //Declare a Socket object
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // Create a ByteArrayOutputStream to store received data
        byte[] buffer = new byte[1024]; // Small buffer for reading data from the server
        
        try {
            // Connect to the server
            socket = new Socket(hostname, port);
            
            // Send data to the server
            OutputStream outToServer = socket.getOutputStream();
            outToServer.write(toServerBytes);
            
            // Receive data from the server
            InputStream inFromServer = socket.getInputStream();
            int bytesRead;
            while ((bytesRead = inFromServer.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); // Write received data to the ByteArrayOutputStream
            }
        } finally {
            if (socket != null) {
                socket.close(); // Close the socket when done
            }
        }
        
        return outputStream.toByteArray(); // Convert ByteArrayOutputStream to byte array and return
    }
}