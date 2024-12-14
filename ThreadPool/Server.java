import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private final ExecutorService threadPool;

    public Server(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public void handleClient(Socket clientSocket) {
        try (
            BufferedReader fromSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true);
            clientSocket
        ) {
            System.out.println("Handling client from: " + clientSocket.getInetAddress());
            
            // Send welcome message
            toSocket.println("Welcome! Type 'TIME' for the current time or anything else to echo it back. Type 'EXIT' to disconnect.");

            String clientMessage;
            while ((clientMessage = fromSocket.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);
                
                if (clientMessage.equalsIgnoreCase("EXIT")) {
                    toSocket.println("Goodbye!");
                    break;
                } else if (clientMessage.equalsIgnoreCase("TIME")) {
                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                    toSocket.println("Current Server Time: " + currentTime);
                } else {
                    toSocket.println("Echo: " + clientMessage);
                }
            }
        } catch (IOException ex) {
            System.err.println("Error handling client: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8010;
        int poolSize = 10;
        Server server = new Server(poolSize);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(0);  // No timeout
            System.out.println("Server is listening on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from: " + clientSocket.getInetAddress());
                    
                    server.threadPool.execute(() -> server.handleClient(clientSocket));
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("Server error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            System.out.println("Shutting down server...");
            server.threadPool.shutdown();
            try {
                if (!server.threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    server.threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.threadPool.shutdownNow();
            }
        }
    }
}