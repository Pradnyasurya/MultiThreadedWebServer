import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public void runInteractiveClient() {
        try (Socket socket = new Socket("localhost", 8010);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Server: " + in.readLine());

            String userInput;
            while (true) {
                System.out.print("Enter command (TIME/'your message'/EXIT): ");
                userInput = systemIn.readLine();

                if (userInput == null || userInput.equalsIgnoreCase("EXIT")) {
                    out.println("EXIT");
                    System.out.println("Server: " + in.readLine());
                    break;
                }
                out.println(userInput);

                String response = in.readLine();
                System.out.println("Server: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client().runInteractiveClient();

        // for (int i = 0; i < 3; i++) {
        //     new Thread(() -> new Client().runInteractiveClient()).start();
        // }

    }
}