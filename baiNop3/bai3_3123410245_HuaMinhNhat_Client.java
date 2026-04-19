package baiNop3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class bai3_3123410245_HuaMinhNhat_Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    // Cờ chung để kiểm soát trạng thái chạy của cả hai luồng
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        System.out.println("Đang kết nối đến server " + SERVER_ADDRESS + ":" + SERVER_PORT);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter writer = new PrintWriter(socket.getOutputStream(), true); // autoFlush=true
                 Scanner consoleScanner = new Scanner(System.in)) {
            System.out.println("Đã kết nối thành công. Gõ 'bye' để thoát.");

            // Client cũng cần multithread --> tạo thread riêng để lắng nghe tin nhắn
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    // Chỉ lặp khi đang chạy & socket chưa bị đóng từ phía này
                    while (running.get() && !socket.isInputShutdown()) {
                        serverMessage = reader.readLine();
                        if (serverMessage == null) {
                            System.out.println("\n*** Server đã đóng kết nối. ***");
                            running.set(false); // Dừng luồng main nếu đang chạy
                            break;
                        }
                        System.out.println("\n<< " + serverMessage);
                        if (running.get()) { // Chỉ in dấu nhắc nếu client vẫn đang chạy
                            System.out.print(">> Bạn: ");
                        }
                    }
                } catch (SocketException e) {
                    // Xử lý lỗi khi socket bị đóng đột ngột hoặc có vấn đề mạng
                    if (running.get()) {
                        System.out.println("\n*** Kết nối đã bị đóng hoặc lỗi (SocketException). ***");
                        running.set(false); // Dừng luồng main
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("\n*** Lỗi khi đọc từ server: " + e.getMessage() + " ***");
                        running.set(false); // Dừng luồng main
                    }
                } finally {
                    running.set(false); // Đảm bảo luồng main cũng dừng khi listener kết thúc
                    System.out.println("\n*** Luồng lắng nghe kết thúc. ***");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            // Main thread gửi dữ liệu
            System.out.print(">> Bạn: ");
            while (running.get()) {
                if (!consoleScanner.hasNextLine()) {
                    running.set(false); // Trường hợp input bị đóng thì báo hiệu kết thúc luôn thread main
                    break;
                }
                String userInput = consoleScanner.nextLine();

                if (writer.checkError()) {
                    System.out.println("\n*** Lỗi khi gửi tin nhắn. Kết nối có thể đã mất. ***");
                    running.set(false); // Dừng cả hai luồng
                    break;
                }

                // Gửi tin nhắn
                writer.println(userInput);

                // Xử lý lệnh "bye"
                if ("bye".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("Đang ngắt kết nối...");
                    running.set(false); // Đặt cờ dừng cho cả hai luồng
                    break;
                }
            }
        } catch (UnknownHostException ex) {
            System.err.println("Không tìm thấy server: " + SERVER_ADDRESS);
        } catch (IOException ex) {
            System.err.println("Không thể kết nối tới server hoặc lỗi I/O: " + ex.getMessage());
        } finally {
            running.set(false); // Đảm bảo cờ được đặt false khi thoát
            System.out.println("\nChương trình client kết thúc.");
        }
    }
}
