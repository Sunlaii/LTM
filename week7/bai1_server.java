package week7;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
import org.jsoup.Jsoup;
import org.json.JSONArray;
import org.json.JSONObject;

public class bai1_3123410245_HuaMinhNhat_Server {
    public static void main(String[] args) {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server dang chay o port " + port + "...");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    String url = in.readLine();
                    if (url != null && !url.isEmpty()) {
                        System.out.println("Nhan link tu client: " + url);
                        GuiThongTinSP(url, out);
                    }
                } catch (Exception e) {
                    System.out.println("Loi xu ly client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void GuiThongTinSP(String url, PrintWriter out) {
        try {
            // Lay ID san pham
            Pattern pattern = Pattern.compile("p(\\d+)\\.html");
            Matcher matcher = pattern.matcher(url);
            String productId = "";

            if (matcher.find()) {
                productId = matcher.group(1);
            } else {
                out.println("Loi tra cuu thong tin, vui long thu lai.");
                out.println("[END]");
                return;
            }

            // 1. GOI API LAY THONG TIN SAN PHAM CO BAN
            String productApi = "https://tiki.vn/api/v2/products/" + productId;
            String productJsonStr = Jsoup.connect(productApi)
                                         .ignoreContentType(true)
                                         .userAgent("Mozilla/5.0")
                                         .execute()
                                         .body();
            JSONObject productJson = new JSONObject(productJsonStr);
            String name = productJson.getString("name");
            long price = productJson.getLong("price");
            double ratingAverage = productJson.optDouble("rating_average", 0.0);
            int reviewCount = productJson.optInt("review_count", 0);

            out.println("Ten san pham: " + name);
            out.println("Gia: " + String.format("%,d", price) + " VND");
            out.println("San pham co " + reviewCount + " review voi danh gia trung binh " + ratingAverage + " sao");
            out.println("Duoi day la cac review ve SP tren Tiki:");

            // 2. GOI API LAY DANH SACH REVIEW
            String reviewApi = "https://tiki.vn/api/v2/reviews?product_id=" + productId;
            String reviewJsonStr = Jsoup.connect(reviewApi)
                                        .ignoreContentType(true)
                                        .userAgent("Mozilla/5.0")
                                        .execute()
                                        .body();
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewsArray = reviewJson.optJSONArray("data");

            if (reviewsArray != null && reviewsArray.length() > 0) {
                // Lay 10 danh gia gan nhat ve san pham (hoac it hon neu khong du 10)
                //(khong lay ten nguoi dung)
                int count = Math.min(10, reviewsArray.length());
                for (int i = 0; i < count; i++) {
                    JSONObject review = reviewsArray.getJSONObject(i);
                    String content = review.optString("content");
                    if (content == null || content.trim().isEmpty()) {
                        content = "Khong co noi dung danh gia";
                    }
                    out.println((i + 1) + ". " + content);
                }
            } else {
                out.println("San pham chua co danh gia nao.");
            }

            // Gui co bao hieu ket thuc
            out.println("[END]");

        } catch (Exception e) {
            out.println("Loi khi lay thong tin san pham: " + e.getMessage());
            out.println("[END]");
        }
    }
}
