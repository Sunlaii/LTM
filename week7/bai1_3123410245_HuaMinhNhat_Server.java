package week7;
import java.io.*;
import java.net.*;
import java.util.regex.*;
import org.jsoup.Jsoup;
import org.json.JSONObject;

public class bai1_3123410245_HuaMinhNhat_Server {
    public static void main(String[] args) {
        int port = 5000; // Dat port cho Server
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server dang chay o port " + port + "...");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    String url = in.readLine();
                    if (url != null && !url.isEmpty()) {
                        System.out.println("Nhan link tu client: " + url);
                        String result = GetTikiLink(url);
                        out.println(result);
                    }
                } catch (Exception e) {
                    System.out.println("Loi xu ly client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String GetTikiLink(String url) {
        try {
            // Trich xuat ID san pham tu URL Tiki
            Pattern pattern = Pattern.compile("p(\\d+)\\.html");
            Matcher matcher = pattern.matcher(url);
            String productId = "";

            if (matcher.find()) {
                productId = matcher.group(1);
            } else {
                return "Loi: Khong tim thay ID san pham trong link cung cap.";
            }

            // Goi API cua Tiki
            String apiUrl = "https://tiki.vn/api/v2/products/" + productId;
            String jsonStr = Jsoup.connect(apiUrl)
                                  .ignoreContentType(true)
                                  .userAgent("Mozilla/5.0")
                                  .execute()
                                  .body();

            // Phan tich JSON bang thu vien json
            JSONObject json = new JSONObject(jsonStr);
            String name = json.getString("name");
            long price = json.getLong("price");

            // Tra ve dinh dang: Ten san pham | Gia VND
            return "Ten: " + name + " | Gia: " + String.format("%,d", price) + " VND";

        } catch (Exception e) {
            return "Loi khi lay thong tin san pham: " + e.getMessage();
        }
    }
}
