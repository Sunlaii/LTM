package week9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 1234;

        try (
            Socket socket = new Socket(serverAddress, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Da ket noi thanh cong toi Server TCP tai port " + port);

            String userInput;
            while (true) {
                System.out.print("Nhap ki tu: ");
                userInput = stdIn.readLine();

                if (userInput == null) break;

                // Gui du lieu len Server
                out.println(userInput);


                // Nhan va hien thi phan hoi IN HOA tu Server
                String response = in.readLine();
                if (response != null) {
                    System.out.println("Server tra ve: " + response);
                } else {
                    System.out.println("Server da dong ket noi.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Khong the ket noi den may chu: " + e.getMessage());
        }
    }
}
