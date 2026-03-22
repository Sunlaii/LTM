package week7;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class bai1_3123410245_HuaMinhNhat_Client {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 5000; // Dam bao khop voi port da thiet lap o Server

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Nhap link Tiki: ");
            String url = scanner.nextLine();

            // Gui link cho Server
            out.println(url);

            // Nhan ket qua tu Server va in ra
            String response = in.readLine();
            System.out.println("=> Ket qua: " + response);

        } catch (IOException e) {
            System.out.println("Loi ket noi den server: " + e.getMessage());
        }
    }
}
