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

// Khai bao thu vien org.json
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
            System.out.println("Server da chuc nang dang chay o port: " + port);
            while (true) {
                String receivedData = receivedData(socket);
                System.out.println("Server nhan yeu cau: " + receivedData);

                if (receivedData.equalsIgnoreCase("exit")) {
                    System.out.println("Server dong ket noi do Client yeu cau.");
                    break;
                }

                // Tach chuoi boi cac khoang trang (ho tro nhieu khoang trang)
                String[] parts = receivedData.split("\\s+");
                String response = "";

                if (parts.length > 0) {
                    String command = parts[0].toLowerCase();

                    // XU LY LENH: weather
                    if (command.equals("weather") && parts.length >= 2) {
                        // Lay toan bo phan dia danh phia sau chu "weather" (bo qua 7 ky tu dau tien)
                        String locationName = receivedData.substring(7).trim();
                        response = getWeatherByLocation(locationName);
                    }
                    // XU LY LENH: convert
                    else if (command.equals("convert") && parts.length == 4) {
                        try {
                            int sourceBase = Integer.parseInt(parts[1]);
                            int targetBase = Integer.parseInt(parts[2]);
                            String numberStr = parts[3];

                            // Kiem tra gioi han cua co so (Java ho tro tu co so 2 den 36)
                            if (sourceBase < 2 || sourceBase > 36 || targetBase < 2 || targetBase > 36) {
                                response = "Loi: Co so ho tro phai nam trong khoang tu 2 den 36.";
                            } else {
                                // Buoc 1: Chuyen so tu co so nguon ve he thap phan (Decimal)
                                long decimalValue = Long.parseLong(numberStr, sourceBase);

                                // Buoc 2: Chuyen tu he thap phan sang he co so dich
                                String convertedNumber = Long.toString(decimalValue, targetBase).toUpperCase();

                                response = "Kết quả đổi " + numberStr + " từ cơ số " + sourceBase + "sang cơ số " + targetBase + " là: " + convertedNumber;
                            }
                        } catch (NumberFormatException e) {
                            // Bat loi neu nguoi dung nhap sai co so (VD: he 2 ma nhap so 9) hoac chua chu cai rac
                            response = "Loi: So hoac co so khong hop le.";
                        }
                    }
                    // XU LY KHI NHAP SAI LENH
                    else {
                        response = "Cu phap khong hop le. Cac lenh ho tro:\n"
                                 + " 1. Tra cuu thoi tiet: weather <dia danh>\n"
                                 + " 2. Chuyen co so     : convert <co_so_nguon> <co_so_dich> <so>";
                    }
                } else {
                    response = "Yeu cau khong duoc de rong.";
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
            String searchLocation = locationName + " Vietnam";
            String encodedLocation = URLEncoder.encode(searchLocation, StandardCharsets.UTF_8.toString()).replace("+", "%20");

            String weatherUrl = "https://api.tomorrow.io/v4/weather/realtime?location=" + encodedLocation + "&apikey=" + TOMORROW_API_KEY;

            String weatherResponse = getApiResponse(weatherUrl);
            JSONObject responseObject = new JSONObject(weatherResponse);

            if (responseObject.has("code")) {
                String message = responseObject.optString("message", "Loi tu Tomorrow.io API.");
                return "Loi khi tra cuu (" + locationName + "): " + message;
            }

            JSONObject data = responseObject.getJSONObject("data");
            JSONObject values = data.getJSONObject("values");
            double temperature = values.getDouble("temperature");

            return "Nhiet do hien tai tai " + locationName + " la: " + temperature + " do C";

        } catch (Exception e) {
            e.printStackTrace();
            return "Loi he thong xu ly JSON khi tra cuu: " + e.getMessage();
        }
    }

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
