package week10;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        int port = 1234;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server dang khoi chay va lang nghe tai port " + port + "...");

            while (true) {
                // Main thread cho client ket noi
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n[+] Co Client moi ket noi tu: " + clientSocket.getInetAddress().getHostAddress() + " (Port giao tiep: " + clientSocket.getPort() + ")");

                // Tao va bat dau mot Thread moi de xu ly doc lap cho client nay
                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Loi Server: " + e.getMessage());
        }
    }
}
