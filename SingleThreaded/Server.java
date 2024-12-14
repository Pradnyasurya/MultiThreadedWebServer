import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    

    public void run() throws IOException{

        int port = 8180;

        ServerSocket serverSocket = new ServerSocket(port);

        serverSocket.setSoTimeout(10000);

        while (true) {
            System.out.println("Server is listening on port" + port);
            Socket acceptedSocket = serverSocket.accept();
            System.out.println("Connection accepted from "+ acceptedSocket.getRemoteSocketAddress());
            PrintWriter toClient = new PrintWriter(acceptedSocket.getOutputStream());
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(acceptedSocket.getInputStream()));

            toClient.println("Hello from the server");
            toClient.close();
            fromClient.close();
            acceptedSocket.close();
        }
    }

    public static void main(String[] args) {

        Server server = new Server();

        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }


        
    }
}
