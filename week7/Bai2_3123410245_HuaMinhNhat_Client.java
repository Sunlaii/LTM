package week7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Bai2_3123410245_HuaMinhNhat_Client {
    private String host;
    private int port;
    private int bufferSize;

    public Bai2_3123410245_HuaMinhNhat_Client(String host, int port, int bufferSize) {
        this.host = host;
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(5000); // Dat thoi gian timeout
            InetAddress address = InetAddress.getByName(host);

            System.out.println("=== CHUONG TRINH TRA CUU THOI TIET TP.HCM ===");
            System.out.println("Cac lua chon:");
            System.out.println(" Nhap '1'  : De tra cuu thoi tiet 1 ngay");
            System.out.println(" Nhap '7'  : De tra cuu thoi tiet 7 ngay");
            System.out.println(" Nhap 'exit': De thoat chuong trinh");
            System.out.println("=============================================");

            while (true) {
                String input = getInput(scanner);
                sendData(socket, address, input);

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Client da ket thuc.");
                    break;
                }

                try {
                    String receivedData = receiveData(socket);
                    System.out.println("=> KET QUA: " + receivedData + "\n");
                } catch (IOException e) {
                    System.out.println("Loi: Khong nhan duoc phan hoi tu Server (Timeout).");
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private String getInput(Scanner scanner) {
        System.out.print("Nhap lua chon cua ban (1/7): ");
        return scanner.nextLine().trim();
    }

    private void sendData(DatagramSocket socket, InetAddress address, String input) throws IOException {
        byte[] dataBytes = input.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, address, port);
        socket.send(packet);
    }

    private String receiveData(DatagramSocket socket) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(packet);
        byte[] dataBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        return new String(dataBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        Bai2_3123410245_HuaMinhNhat_Client client = new Bai2_3123410245_HuaMinhNhat_Client("localhost", 1234, 1024);
        client.start();
    }
}
