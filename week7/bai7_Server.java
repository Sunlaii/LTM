package week7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class bai7_Server {
    private int port;
    private int bufferSize;
    private DatagramPacket receivePacket; // chia se giua sendData va receivedData

    public bai7_Server(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start(){
        try(DatagramSocket socket = new DatagramSocket(port)){
            while(true){
                String receivedData = receivedData(socket); // nen bat SocketTimeoutException o day
                System.out.println("Server nhan: " + receivedData);
                if(receivedData.equalsIgnoreCase("exit")){
                    System.out.println("Server dong ket noi.");
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

    private void sendData(DatagramSocket socket, String receivedData) throws IOException {
        String response = receivedData.toUpperCase();
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(responseBytes, responseBytes.length, receivePacket.getAddress(), receivePacket.getPort());
        socket.send(packet);
    }

    public static void main(String[] args) {
        bai7_Server server = new bai7_Server(1234, 1024);
        server.start();
    }
}
