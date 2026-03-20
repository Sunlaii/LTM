package week7;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class W5_Server {
    private int port;

    public W5_Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP Server đang lắng nghe tại port " + port);
            while (true) {
                Socket socket = server.accept();
                handleClient(socket);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi tạo server socket: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        System.out.println("Đã chấp nhận kết nối từ client: " + socket.getRemoteSocketAddress());

        // Sử dụng UTF-8 để không bị lỗi tiếng Việt
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String categoryUrl;
            while ((categoryUrl = reader.readLine()) != null) {
                if (categoryUrl.equalsIgnoreCase("bye")) {
                    System.out.println("Client đã ngắt kết nối.");
                    break;
                }

                System.out.println("Đang xử lý link danh mục: " + categoryUrl);

                try {
                    // Kết nối tới trang danh mục của Tiki
                    Document doc = Jsoup.connect(categoryUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(15000)
                            .get();

                    // Tìm tất cả các khối sản phẩm (Tiki thường dùng các class/thuộc tính này cho item sản phẩm)
                    Elements products = doc.select("a.product-item, [data-view-id='product_list_item']");

                    if (products.isEmpty()) {
                        writer.println("Không tìm thấy sản phẩm nào hoặc link không đúng chuẩn danh mục Tiki.");
                    } else {
                        writer.println("--- DANH SÁCH SẢN PHẨM ---");
                        int count = 1;
                        for (Element product : products) {
                            // Lấy tên (thường nằm trong thẻ h3 hoặc div có class name)
                            Element nameEl = product.selectFirst("h3, .name");
                            String name = (nameEl != null) ? nameEl.text() : "Chưa cập nhật tên";

                            // Lấy giá (thường nằm trong div có class chứa chữ price)
                            Element priceEl = product.selectFirst(".price-discount__price");
                            String price = (priceEl != null) ? priceEl.text() : "Chưa cập nhật giá";

                            writer.println(count + ". " + name + " | Giá: " + price);
                            count++;

                            // Giới hạn gửi về khoảng 20 sản phẩm đầu tiên để tránh console quá dài
                            if (count > 20) break;
                        }
                    }
                } catch (Exception e) {
                    writer.println("Lỗi khi cào dữ liệu từ Tiki: " + e.getMessage());
                }

                writer.println("<END>"); // Dấu hiệu kết thúc phản hồi
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối từ client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        W5_Server server = new W5_Server(12345);
        server.start();
    }
}
