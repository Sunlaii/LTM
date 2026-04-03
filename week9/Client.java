package week9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 1234;
        try (
                Socket socket = new Socket(hostname, port);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server. Enter message (type 'bye' to exit):");
            String userInput;
            do {
                System.out.print("Client: ");
                userInput = scanner.nextLine();
                writer.println(userInput);
                String serverResponse = reader.readLine();
                System.out.println("Server: " + serverResponse);
            } while (!"bye".equalsIgnoreCase(userInput.trim()));
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
