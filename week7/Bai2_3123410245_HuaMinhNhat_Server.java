package week7;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// Khai bao thu vien org.json (Dung file json-20230618.jar trong thu muc lib)
import org.json.JSONArray;
import org.json.JSONObject;

public class Bai2_3123410245_HuaMinhNhat_Server {
    private int port;
    private int bufferSize;
    private DatagramPacket receivePacket;

    // Khai bao API Key va toa do TP.HCM
    private static final String API_KEY = "WXiL3IW2Ss0LkLdhVE1QTQaQ29fD6BMZ";
    private static final String LOCATION = "10.8231,106.6297";

    public Bai2_3123410245_HuaMinhNhat_Server(int port, int bufferSize) {
        this.port = port;
        this.bufferSize = bufferSize;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Server tra cuu thoi tiet dang chay o port: " + port);
            while (true) {
                String receivedData = receivedData(socket);
                System.out.println("Server nhan yeu cau tra cuu: " + receivedData + " ngay");

                if (receivedData.equalsIgnoreCase("exit")) {
                    System.out.println("Server dong ket noi.");
                    break;
                }

                sendData(socket, receivedData);
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

    // Goi API toi Tomorrow.io va lay ra so ngay tuong ung
    private String getWeatherForecastFromAPI(int daysRequested) {
        try {
            // Su dung endpoint du bao thoi tiet theo ngay (timesteps=1d)
            String urlString = "https://api.tomorrow.io/v4/weather/forecast?location="
                                + LOCATION + "&timesteps=1d&apikey=" + API_KEY;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // Timeout 5 giay
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return "Loi tu API thoi tiet: HTTP " + conn.getResponseCode();
            }

            // Doc du lieu JSON tra ve tu API


            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            //lưu lại json
            try (FileWriter file = new FileWriter("weather_forecast.json")) {
                file.write(content.toString());
                file.flush();
                System.out.println("Da luu file JSON thanh cong: weather_forecast.json");
            } catch (IOException e) {
                System.err.println("Loi khi ghi file JSON: " + e.getMessage());
            }


            // Phan tich JSON
            JSONObject jsonObject = new JSONObject(content.toString());
            JSONArray dailyArray = jsonObject.getJSONObject("timelines").getJSONArray("daily");

            StringBuilder result = new StringBuilder();
            result.append("Du bao thoi tiet TP.HCM:\n");

            // Xac dinh so ngay tra ve
            int daysToReturn = Math.min(daysRequested, dailyArray.length());

            for (int i = 0; i < daysToReturn; i++) {
                JSONObject dayData = dailyArray.getJSONObject(i);
                String time = dayData.getString("time").substring(0, 10); // Lay chuoi YYYY-MM-DD
                JSONObject values = dayData.getJSONObject("values");

                double tempAvg = values.optDouble("temperatureAvg", 0);
                double tempMax = values.optDouble("temperatureMax", 0);
                double tempMin = values.optDouble("temperatureMin", 0);

                result.append("- Ngay ").append(time).append(": ")
                      .append("Nhiet do TB: ").append(Math.round(tempAvg)).append("°C ")
                      .append("(Min: ").append(Math.round(tempMin)).append("°C - ")
                      .append("Max: ").append(Math.round(tempMax)).append("°C)\n");
            }

            if (daysRequested > dailyArray.length()) {
                result.append("\n(Luu y: API mien phi cua Tomorrow.io chi ho tro toi da ")
                      .append(dailyArray.length()).append(" ngay tiep theo)");
            }

            return result.toString();

        } catch (Exception e) {
            return "Khong the lay du lieu thoi tiet luc nay. Loi: " + e.getMessage();
        }
    }

    private String getWeatherForecast(String choice) {
        switch (choice) {
            case "1":
                return getWeatherForecastFromAPI(1);
            case "7":
                return getWeatherForecastFromAPI(7);
            default:
                return "Lua chon khong hop le. Vui long chon 1 hoac 7.";
        }
    }

    private void sendData(DatagramSocket socket, String receivedData) throws IOException {
        String response = getWeatherForecast(receivedData);
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(
                responseBytes,
                responseBytes.length,
                receivePacket.getAddress(),
                receivePacket.getPort()
        );
        socket.send(packet);
    }

    public static void main(String[] args) {
        // Tang bufferSize len 4096 de chua du chuoi du bao thoi tiet nhieu ngay ma khong bi cat ngang
        Bai2_3123410245_HuaMinhNhat_Server server = new Bai2_3123410245_HuaMinhNhat_Server(1234, 4096);
        server.start();
    }
}
