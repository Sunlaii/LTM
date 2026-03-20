package week7;
import java.nio.charset.StandardCharsets;
import java.net.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class W5_Server {
    public static void main(String[] args) {
        int port = 1234;
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("UDP Server đang chờ nhận link Tiki tại cổng " + port + "...");

            byte[] receiveData = new byte[1024];

            while (true) {
                // 1. Nhận link từ Client
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String link = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                System.out.println("\nNhận được link từ Client: " + link);

                String responseMsg = "";

                try {
                    // 2. Sử dụng Jsoup để kết nối và lấy dữ liệu trang web
                    // Thêm User-Agent để tránh bị Tiki chặn do tưởng là bot
                    Document doc = Jsoup.connect(link)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .timeout(10000)
                            .get();

                    // 3. Lấy tên sản phẩm (Thường nằm trong thẻ h1 hoặc title)
                    String productName = "Không tìm thấy tên";
                    Element nameElement = doc.selectFirst("h1"); // H1 thường chứa tên sản phẩm trên Tiki
                    if (nameElement != null) {
                        productName = nameElement.text();
                    } else {
                        productName = doc.title(); // Fallback dùng thẻ title
                    }

                    // 4. Lấy giá sản phẩm (Tìm theo class CSS chứa giá)
                    String productPrice = "Không tìm thấy giá";
                    // Class này có thể thay đổi tùy theo bản cập nhật UI của Tiki (.product-price__current-price là class phổ biến)
                    Element priceElement = doc.selectFirst(".product-price__current-price");
                    if (priceElement != null) {
                        productPrice = priceElement.text();
                    }

                    responseMsg = "Tên sản phẩm: " + productName + "\nGiá: " + productPrice;
                    System.out.println("Đã cào xong dữ liệu, chuẩn bị gửi về Client...");

                } catch (Exception ex) {
                    responseMsg = "Lỗi khi truy cập link hoặc bóc tách dữ liệu: " + ex.getMessage();
                }

                // 5. Gửi dữ liệu (Tên + Giá) về lại cho Client
                byte[] sendData = responseMsg.getBytes(StandardCharsets.UTF_8);
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
