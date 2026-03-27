package week7;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class bai1_3123410245_HuaMinhNhat_Client {
    public static void main(String[] args) {

        String host = "127.0.0.1";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            while (true) { // Bat dau vong lap
                System.out.print("\nNhap link Tiki ( go 'exit' de thoat): ");
                String url = scanner.nextLine();

                if ("exit".equalsIgnoreCase(url)) {
                    System.out.println(" thoat chuong trinh.");
                    break;
                }

                // Gui link cho Server
                out.println(url);

                System.out.println("\n=> KET QUA TU SERVER:");

                // Doc nhieu dong tu Server cho toi khi gap co [END]
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.equals("[END]")) {
                        break;
                    }
                    System.out.println(response);
                }
            }

        } catch (IOException e) {
            System.out.println("Loi tra cuu thong tin, vui long thu lai.");
        }
    }
}
