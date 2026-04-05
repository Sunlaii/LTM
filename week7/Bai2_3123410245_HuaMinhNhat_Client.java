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
        // Them StandardCharsets.UTF_8.name() de doc tieng Viet tu ban phim
        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {

            socket.setSoTimeout(5000);
            InetAddress address = InetAddress.getByName(host);

            System.out.println("=== CHUONG TRINH TRA CUU THOI TIET ===");
            System.out.println("Cac lua chon:");
            System.out.println(" Nhap 'weather <dia danh>' : De tra cuu nhiet do hien tai.");
            System.out.println("                             Vi du: weather ha giang");
            System.out.println(" Nhap 'exit'               : De thoat chuong trinh");
            System.out.println("=============================================");

            while (true) {
                String input = getInput(scanner);

                if (input.equalsIgnoreCase("exit")) {
                    sendData(socket, address, input);
                    System.out.println("Client da ket thuc.");
                    break;
                }

                sendData(socket, address, input);

                try {
                    String receivedData = receiveData(socket);
                    System.out.println("=> KET QUA: " + receivedData + "\n");
                } catch (IOException e) {
                    System.out.println("=> Loi: Khong nhan duoc phan hoi tu Server (Timeout).\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Loi socket: " + e.getMessage());
        }
    }

    private String getInput(Scanner scanner) {
        System.out.print("Nhap yeu cau cua ban: ");
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
        Bai2_3123410245_HuaMinhNhat_Client client = new Bai2_3123410245_HuaMinhNhat_Client("localhost", 9876, 1024);
        client.start();
    }
}
