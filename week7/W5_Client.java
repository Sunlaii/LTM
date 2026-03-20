package week7;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
public class W5_Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        try (DatagramSocket clientSocket = new DatagramSocket();
             Scanner sc = new Scanner(System.in)) {

            InetAddress address = InetAddress.getByName(host);

            // 1. Nhập link Tiki từ bàn phím
            System.out.print("Nhập link sản phẩm Tiki: ");
            String link = sc.nextLine();
            byte[] sendData = link.getBytes(StandardCharsets.UTF_8);

            // 2. Gửi link lên Server
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            clientSocket.send(sendPacket);

            System.out.println("Đang chờ phản hồi từ Server...");

            // 3. Nhận phản hồi (Tên & Giá) từ Server
            // Đặt buffer lớn hơn một chút vì tên sản phẩm có thể dài
            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            // 4. In kết quả
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8  );
            System.out.println("\n--- THÔNG TIN SẢN PHẨM ---");
            System.out.println(response);
            System.out.println("--------------------------");

        } catch (Exception e) {
            System.out.println("Lỗi Client: " + e.getMessage());
        }
    }
}
