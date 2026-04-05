package week7;

import java.io.BufferedReader;
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
            System.out.println("Server tra cứu thoi tiet dang chay o port: " + port);
            while (true) {
                String receivedData = receivedData(socket);
                System.out.println("Server nhan yeu cau: " + receivedData);

                if (receivedData.equalsIgnoreCase("exit")) {
                    System.out.println("Server dong ket noi do Client yeu cau.");
                    break;
                }

                // Cat chuoi thanh 2 phan: [0] = lenh, [1] = phan dia danh phia sau
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
            // Them chu "Vietnam" (KHONG CO DAU PHAY) de API biet tim o VN, khong bi nham sang chau Au
            String searchLocation = locationName + " Vietnam";

            // Encode va doi dau "+" thanh "%20" de giu dung khoang trang
            String encodedLocation = URLEncoder.encode(searchLocation, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            // In ra chuoi da encode de debug
            System.out.println("[DEBUG] Chuoi da encode gui len API: " + encodedLocation);
            // Goi API Realtime cua Tomorrow.io
            String weatherUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" + encodedLocation + "&apikey=" + TOMORROW_API_KEY;

            String weatherResponse = getApiResponse(weatherUrl);
            JSONObject responseObject = new JSONObject(weatherResponse);

            // Kiem tra xem API co tra ve loi khong
            if (responseObject.has("code")) {
                String message = responseObject.optString("message", "Loi tu Tomorrow.io API.");
                return "Loi khi tra cuu (" + locationName + "): " + message;
            }

            // Phan tich lay truong "temperature"
            JSONObject data = responseObject.getJSONObject("data");
            JSONObject values = data.getJSONObject("values");
            double temperature = values.getDouble("temperature");

            return "Nhiet do hien tai tai " + locationName + " la: " + temperature + " do C";

        } catch (Exception e) {
            e.printStackTrace();
            return "Loi he thong xu ly JSON khi tra cuu: " + e.getMessage();
        }
    }

    // Ham goi API an toan (bat ca InputStream lan ErrorStream de khong bi crash ung dung)
    private String getApiResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        InputStream is;

        if (status >= 200 && status < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
            if (is == null) {
                return "{\"code\": \"" + status + "\", \"message\": \"HTTP Error\"}";
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
        Bai2_3123410245_HuaMinhNhat_Server server = new Bai2_3123410245_HuaMinhNhat_Server(9876, 1024);
        server.start();
    }
}
