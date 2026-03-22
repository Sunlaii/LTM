package week7;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Bai2_Server {
    private int port;
    private int bufferSize;
    private DatagramPacket receivePacket; // chia sẻ giữa sendData và receivedData

    public Bai2_Server(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start(){
        try(DatagramSocket socket = new DatagramSocket(port)){
            while(true){
                String receivedData = receivedData(socket); // nên bắt SocketTimeoutException ở đây
                System.out.println("Server nhận: " + receivedData);
                if(receivedData.equalsIgnoreCase("exit")){
                    System.out.println("Server đóng kết nối.");
                    break;
                }
                sendData(socket, receivedData);
            }
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    private String receivedData(DatagramSocket socket) throws IOException {
        receivePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(receivePacket);
        byte[] receivedBytes = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
        return new String(receivedBytes, StandardCharsets.UTF_8);
    }

  private int digitSum(int num) {
    int sum = 0;

    // Lặp cho đến khi số num bị cắt hết (bằng 0)
    while (num > 0) {
        sum += num % 10; // Lấy chữ số cuối cùng cộng vào tổng
        num /= 10;       // Cắt bỏ chữ số cuối cùng đi
    }

    return sum;
}

    private int findNumbersWithDigitSum(int n) {
        //tìm tổng số lượng số có tổng chữ số bằng n trong file data.txt
        int count = 0;
        try (Scanner scanner = new Scanner(new File("week7/data.txt"))) {
            while (scanner.hasNextInt()) {
                int number = scanner.nextInt();
                if (digitSum(number) == n) {
                    count++;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + e.getMessage());
        }
        return count;
    }

    private void sendData(DatagramSocket socket, String receivedData) throws IOException {
        try {
            int n = Integer.parseInt(receivedData.trim());
            int count = findNumbersWithDigitSum(n);
            String response = "Số lượng số có tổng chữ số bằng " + n + " là: " + count;
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(responseBytes, responseBytes.length, receivePacket.getAddress(), receivePacket.getPort());
            socket.send(packet);
        } catch (NumberFormatException e) {
            String errorMessage = "Dữ liệu không hợp lệ: " + receivedData;
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket errorPacket = new DatagramPacket(errorBytes, errorBytes.length, receivePacket.getAddress(), receivePacket.getPort());
            socket.send(errorPacket);
        }
    }

    public static void main(String[] args) {
        Bai2_Server server = new Bai2_Server(1234, 1024);
        server.start();
    }
}
