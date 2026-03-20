package week7;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class W5_Client {
    private String host;
    private int port;

    public W5_Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Đã kết nối đến server " + socket.getRemoteSocketAddress());
            startCommunication(socket);
        } catch (IOException e) {
            System.err.println("Lỗi kết nối đến server: " + e.getMessage());
        }
    }

    private void startCommunication(Socket socket) {
        // Cấu hình UTF-8 ở cả Scanner (đọc phím) và Stream (đọc mạng)
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String userInput;
            while (true) {
                System.out.print("Nhập link danh mục Tiki (hoặc gõ 'bye' để thoát): ");
                userInput = scanner.nextLine();

                writer.println(userInput); // Gửi URL lên Server

                if (userInput.equalsIgnoreCase("bye")) {
                    System.out.println("Đang đóng kết nối...");
                    break;
                }

                System.out.println("Đang chờ server xử lý và trích xuất dữ liệu...\n");

                // Đọc phản hồi từ Server (nhiều dòng) cho đến khi gặp chữ <END>
                String response;
                while ((response = reader.readLine()) != null) {
                    if (response.equals("<END>")) {
                        break;
                    }
                    System.out.println(response);
                }
                System.out.println("--------------------------------------------------");
            }
        } catch (IOException e) {
            System.out.println("Lỗi gửi/nhận dữ liệu: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        W5_Client client = new W5_Client("localhost", 12345);
        client.start();
    }
}
