import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class Server {
    private final ExecutorService threadPool;
    private volatile boolean isRunning = true;

    public Server(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public void handleClient(Socket clientSocket) {
        try (
            BufferedReader fromSocket = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true)
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
        }
    }

    public static void main(String[] args) {
        int port = 8010;
        int poolSize = 10;
        Server server = new Server(poolSize);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown initiated...");
            server.shutdown();
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (server.isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from: " + clientSocket.getInetAddress());

                    server.threadPool.execute(() -> server.handleClient(clientSocket));
                } catch (SocketException se) {
                    // Break out of the loop if server socket is closed during shutdown
                    if (!server.isRunning) {
                        System.out.println("Server socket closed.");
                        break;
                    }
                    System.err.println("Socket error: " + se.getMessage());
                } catch (IOException ex) {
                    System.err.println("Error accepting connection: " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.err.println("Server error: " + ex.getMessage());
        } finally {
            server.shutdown();
        }
    }

    public void shutdown() {
        isRunning = false; 
        threadPool.shutdown();
        try {
            System.out.println("Waiting for active tasks to finish...");
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown of remaining tasks...");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        System.out.println("Server shutdown complete.");
    }
}
