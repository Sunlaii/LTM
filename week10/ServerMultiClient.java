package week10;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerMultiClient {

    private static final int PORT = 12345;
    // Sử dụng ConcurrentHashMap để quản lý clients
    // Key: clientId (username), Value: ClientHandler
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("Multi-Client Chat Server đang khởi động...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang lắng nghe trên cổng " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Kết nối mới từ: " + clientSocket.getInetAddress().getHostAddress());
                    // Tạo ClientHandler mới và giao cho Executor xử lý
                    // Handler sẽ tự xử lý việc đăng ký trong luồng của nó
                    executorService.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    System.err.println("Lỗi khi chấp nhận kết nối mới: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi nghiêm trọng với ServerSocket: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Đang shutdown ExecutorService và dừng server...");
            shutdownExecutorService();
            System.out.println("Server đã dừng hẳn.");
        }
    }

    /**
     * Đăng ký một client handler mới vào map.
     *
     * @param handler Handler cần đăng ký.
     * @return true nếu đăng ký thành công, false nếu ID đã tồn tại.
     */
    public static boolean addClient(ClientHandler handler) {
        // putIfAbsent là atomic, trả về null nếu key chưa tồn tại và thêm thành công
        return clients.putIfAbsent(handler.getClientId(), handler) == null;
    }

    /**
     * Xóa một client handler khỏi map và thông báo cho những người khác.
     *
     * @param handler Handler cần xóa.
     */
    public static void removeClient(ClientHandler handler) {
        String clientId = handler.getClientId();
        if (clientId != null) {
            // Chỉ xóa nếu giá trị trong map đúng là handler này (tránh race condition)
            boolean removed = clients.remove(clientId, handler);
            if (removed) {
                System.out.println("Client đã bị xóa: " + clientId);
                // Thông báo cho những người còn lại
                broadcastMessage("SERVER: " + clientId + " đã rời khỏi phòng chat.", handler);
            }
        } else {
            System.out.println("Đang xóa client không có ID.");
        }
    }

    /**
     * Gửi tin nhắn đến một client cụ thể.
     *
     * @param targetClientId ID của người nhận.
     * @param message Nội dung tin nhắn.
     * @param sender Handler của người gửi (để định dạng tin nhắn và tránh tự
     * gửi).
     * @return true nếu gửi thành công, false nếu người nhận không tồn tại hoặc
     * là người gửi.
     */
    public static boolean sendPrivateMessage(String targetClientId, String message, ClientHandler sender) {
        ClientHandler recipient = clients.get(targetClientId);
        if (recipient != null) {
            if (recipient == sender) {
                sender.sendMessage("SERVER: Bạn không thể gửi tin nhắn cho chính mình.");
                return false;
            }
            // Định dạng tin nhắn
            String formattedMessage = "[" + sender.getClientId() + " -> private]: " + message;
            recipient.sendMessage(formattedMessage);
            return true;
        } else {
            // Thông báo cho người gửi rằng người nhận không tồn tại
            sender.sendMessage("SERVER: Người dùng '" + targetClientId + "' không online hoặc không tồn tại.");
            return false;
        }
    }

    /**
     * Gửi tin nhắn đến tất cả các client đang kết nối (trừ người gửi).
     *
     * @param message Nội dung tin nhắn cần broadcast.
     * @param sender Handler của người gửi (null nếu gửi từ SERVER).
     */
    public static void broadcastMessage(String message, ClientHandler sender) {
        String senderId = (sender == null) ? "SERVER" : sender.getClientId();
        System.out.println("Broadcasting (" + senderId + "): " + message); // Log trên server
        for (ClientHandler client : clients.values()) {
            // Không gửi lại cho chính người gửi tin nhắn
            if (client != sender) {
                client.sendMessage(message); // Gửi tin nhắn gốc (đã được định dạng trước khi gọi)
            }
        }
    }

    // Helper method để đóng socket nếu cần
    private static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                /* ignore */ }
        }
    }

    // Helper method để shutdown ExecutorService
    private static void shutdownExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService chưa kết thúc.");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
