package week10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private String clientId; // ID sẽ được đặt sau khi client khởi tạo
    private PrintWriter writer;
    private BufferedReader reader;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public String getClientId() {
        return clientId;
    }

    // Gửi tin nhắn tới client mà handler này quản lý
    public void sendMessage(String message) {
        if (running.get() && writer != null && !writer.checkError()) {
            writer.println(message);
        }
    }

    private void closeResources() {
        running.set(false);
        System.out.println("Closing resources for: " + (clientId != null ? clientId : "unregistered"));
        try {
            if (reader != null) {
                reader.close();

            }
        } catch (IOException e) {
            /* ignore */ }
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            /* ignore */ }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();

            }
        } catch (IOException e) {
            /* ignore */ }
        System.out.println("Resources closed for: " + (clientId != null ? clientId : "unregistered"));
    }

    @Override
    public void run() {
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        System.out.println("Khởi tạo thread handler cho client " + clientInfo);

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Đăng ký Client ID
            String requestedId;
            boolean registered = false;
            while (running.get() && !registered) {
                sendMessage("SERVER: Vui lòng nhập tên đăng nhập (không chứa #, không phải 'all'):");
                requestedId = reader.readLine();

                if (requestedId == null) { // Client ngắt kết nối trước khi đăng ký
                    System.out.println("Client " + clientInfo + " disconnected before registration.");
                    running.set(false); // Đặt cờ dừng
                    break;
                }

                requestedId = requestedId.trim();
                if (requestedId.isEmpty() || requestedId.contains("#") || requestedId.equalsIgnoreCase("all")) {
                    sendMessage("SERVER: Tên đăng nhập không hợp lệ. Vui lòng thử lại.");
                    continue;
                }

                // Gán ID và đăng ký vào Map
                this.clientId = requestedId;
                if (ServerMultiClient.addClient(this)) {
                    System.out.println("Client đăng ký thành công: " + this.clientId + " (" + clientInfo + ")");
                    registered = true;
                } else {
                    // ID đã tồn tại --> Đặt lại ID vì đăng ký không thành công
                    this.clientId = null;
                    sendMessage("SERVER: Tên đăng nhập '" + requestedId + "' đã được sử dụng. Vui lòng chọn tên khác.");
                }
            }

            // Nếu thoát vòng lặp mà chưa đăng ký được (do lỗi hoặc client ngắt kết nối)
            if (!registered) {
                System.out.println("Đăng ký thất bại: " + clientInfo);
                return;
            }

            // Chào mừng Client mới và Broadcast
            sendMessage("SERVER: Chào mừng " + this.clientId + "! Bạn đã tham gia phòng chat.");
            sendMessage("SERVER: Gõ 'recipient_id#message' để gửi riêng, 'all#message' để gửi chung, 'bye' để thoát.");
            // Gọi phương thức static của server để broadcast
            ServerMultiClient.broadcastMessage("SERVER: " + this.clientId + " đã tham gia phòng chat.", this);

            // Xử lý tin nhắn chính
            String messageFromClient;
            while (running.get() && (messageFromClient = reader.readLine()) != null) {
                System.out.println("<- [" + this.clientId + "]: " + messageFromClient);
                if ("bye".equalsIgnoreCase(messageFromClient.trim())) {
                    running.set(false);
                    sendMessage("SERVER: Bạn đang rời khỏi phòng chat...");
                    break;
                }
                // Phân tích tin nhắn ---
                String[] parts = messageFromClient.split("#", 2);
                if (parts.length == 2) {
                    String destination = parts[0].trim();
                    String content = parts[1];

                    if ("all".equalsIgnoreCase(destination)) {
                        // Định dạng tin nhắn broadcast và gọi helper của server
                        String broadcastContent = "[" + this.clientId + " -> ALL]: " + content;
                        ServerMultiClient.broadcastMessage(broadcastContent, this);
                    } else {
                        // Gửi tin nhắn riêng tư qua helper của server
                        ServerMultiClient.sendPrivateMessage(destination, content, this);
                    }
                } else {
                    sendMessage("SERVER: Định dạng tin nhắn không hợp lệ. Sử dụng 'all#message' hoặc 'recipient_id#message'.");
                }
            }

        } catch (SocketException e) {
            // Chỉ log nếu client chưa chủ động dừng (ví dụ: không phải do gửi 'bye')
            if (running.get()) {
                System.out.println("SocketException for " + (clientId != null ? clientId : clientInfo) + ": " + e.getMessage() + ". Client disconnected?");
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("IOException for " + (clientId != null ? clientId : clientInfo) + ": " + e.getMessage());
            }
        } finally {
            // Xóa client khỏi map và broadcast rời đi trước khi đóng tài nguyên
            ServerMultiClient.removeClient(this);
            // Đóng tài nguyên
            closeResources();
            System.out.println("Thread Handler của client " + (clientId != null ? clientId : clientInfo) + " đã kết thúc.");
        }
    }
}
