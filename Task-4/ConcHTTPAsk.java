import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConcHTTPAsk {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Port number needs to be provided");
            System.exit(1); // Exiting the program with Error status 
        } 

        int port = Integer.parseInt(args[0]); // Parse:ing the port number that was provided in the command line 

        if (port <= 1024) { // has to be greater than 1024
            System.err.println("Error: Port number must be greater than 1024");
            System.exit(1);
        }

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("The server is established in port: " + port); // Validating an established port.

            while (true) {
                Socket client = server.accept();
                System.out.println("The server has accepted the client connection in port: " + port);

                // Creating a new thread to handle the client connection
                Thread thread = new Thread(new MyRunnable(client));
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
