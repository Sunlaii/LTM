package week7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class bai1_3123410245_HuaMinhNhat_Server {
    private int port;
    private int bufferSize;
    private DatagramPacket receivePacket; // chia sẻ giữa sendData và receivedData

    public bai1_3123410245_HuaMinhNhat_Server(int port, int bufferSize) {
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
   //gọi api tiki để lấy tên và giá sản phẩm từ link đã nhận được rồi gửi lại cho client
   //lấy tên và giá sản phẩm
  
    public static void main(String[] args) {
        bai1_3123410245_HuaMinhNhat_Server server = new bai1_3123410245_HuaMinhNhat_Server(1234, 1024);
        server.start();
    }
}
