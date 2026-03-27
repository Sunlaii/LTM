package week7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class bai7_client {
    private String host;
    private int port;
    private int bufferSize;

    public bai7_client(String host, int port, int bufferSize){
        this.host = host;
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start(){
            try(DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in)){
                socket.setSoTimeout(5000); // dat thoi gian timeout cho receive la 5s
                InetAddress address = InetAddress.getByName(host);
                while(true){
                    String input = getInput(scanner);
                    sendData(socket, address, input);
                    if(input.equalsIgnoreCase("exit")){
                        System.out.println("Client nhan duoc yeu cau ket thuc");
                        break;
                    }
                    String receivedData = receiveData(socket); // nen bat SocketTimeoutException o day
                    System.out.println("Client nhan: " + receivedData);
                }
            }catch(IOException e){
                System.err.println(e.getMessage());
            }
    }
    // Nhan du lieu tu ban phim
    private String getInput(Scanner scanner){
        System.out.print("Nhap du lieu: ");
        return scanner.nextLine();
    }
    // Gui du lieu den Server
    private void sendData(DatagramSocket socket, InetAddress address, String input) throws IOException {
        byte[] dataBytes = input.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, address, port);
        socket.send(packet);
    }
    // Nhận dữ liệu từ Server
    private String receiveData(DatagramSocket socket) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(packet);
        byte[] dataBytes = Arrays.copyOf(packet.getData(), packet.getLength());
        return new String(dataBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        bai7_client client = new bai7_client("127.0.0.1", 1234, 1024);
        client.start();
    }
}
