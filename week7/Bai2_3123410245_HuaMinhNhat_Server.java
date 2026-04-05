package week7;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// Khai bao thu vien org.json
import org.json.JSONArray;
import org.json.JSONObject;

public class Bai2_3123410245_HuaMinhNhat_Server {
    private int port;
    private int bufferSize;
    private DatagramPacket receivePacket;

    // Khai bao API Key
    private static final String TOMORROW_API_KEY = "WXiL3IW2Ss0LkLdhVE1QTQaQ29fD6BMZ";

    public Bai2_3123410245_HuaMinhNhat_Server(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Server tra cuu thoi tiet dang chay o port: " + port);
            while (true) {
                String receivedData = receivedData(socket);
                System.out.println("Server nhan yeu cau: " + receivedData);

                if (receivedData.equalsIgnoreCase("exit")) {
                    System.out.println("Server dong ket noi.");
                    break;
                }

                // Cat chuoi voi ky tu khoang trang, toi da 2 phan: "weather" va "dia danh"
                String[] parts = receivedData.split(" ", 2);
                String response;

                if (parts.length == 2 && parts[0].equalsIgnoreCase("weather")) {
                    String locationName = parts[1].trim();
                    response = getWeatherByLocation(locationName);
                } else {
                    response = "Cu phap khong hop le. Su dung: weather <dia danh> (VD: weather ha giang)";
                }

                sendData(socket, response);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private String receivedData(DatagramSocket socket) throws IOException {
        receivePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(receivePacket);
        byte[] receivedBytes = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
        return new String(receivedBytes, StandardCharsets.UTF_8).trim();
    }

    private String getWeatherByLocation(String locationName) {
        try {
            // Encode dia danh, dac biet ho tro tieng Viet co dau hoac khoang trang
            String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8.toString());
            String weatherUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" + encodedLocation + "&apikey=" + TOMORROW_API_KEY;

            String weatherResponse = getApiResponse(weatherUrl);
            JSONObject responseObject = new JSONObject(weatherResponse);

            // Kiem tra xem API co tra ve loi khong (VD: dia danh khong ton tai)
            if (responseObject.has("code")) {
                String message = responseObject.optString("message", responseObject.optString("type", "Lỗi không xác định từ API."));
                return "Loi tra cuu (" + locationName + "): " + message;
            }

            JSONObject data = responseObject.getJSONObject("data");
            JSONObject values = data.getJSONObject("values");
            double temperature = values.getDouble("temperature");

            JSONObject location = responseObject.optJSONObject("location");
            // Neu API khong co truong name, dung luon ten ma user da nhap
            String name = (location != null && location.has("name")) ? location.getString("name") : locationName;

            return "Nhiet do hien tai o " + name + " la: " + temperature + " do C";

        } catch (Exception e) {
            e.printStackTrace();
            return "Loi he thong khi tra cuu thoi tiet: " + e.getMessage();
        }
    }

    private String getApiResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000); // Thiet lap thoi gian cho de tranh bi treo
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        InputStream is;

        // Neu tra ve 200 OK thi doc tu InputStream, neu loi (400, 401, 429...) thi doc tu ErrorStream
        if (status >= 200 && status < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
            if (is == null) {
                return "{\"code\": \"" + status + "\", \"message\": \"Lỗi HTTP không xác định.\"}";
            }
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }

    private void sendData(DatagramSocket socket, String data) throws IOException {
        byte[] sendData = data.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                receivePacket.getAddress(),
                receivePacket.getPort()
        );
        socket.send(sendPacket);
    }

    public static void main(String[] args) {
        // Port hien tai dang la 9876, hay chac chan file Client cua ban cung dung Port 9876 nay
        Bai2_3123410245_HuaMinhNhat_Server server = new Bai2_3123410245_HuaMinhNhat_Server(9876, 1024);
        server.start();
    }
}
