package nhanh4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Lop TCPServer: Mo hinh Server don gian theo kien truc Client/Server.
 *
 * Nguyen ly hoat dong:
 * 1. Khoi tao ServerSocket lang nghe tai mot cong TCP xac dinh (mac dinh 9090).
 * 2. Phuong thuc accept() se block (cho) cho toi khi co mot TCP Client gui yeu cau ket noi (SYN).
 * 3. Sau khi hoan tat bat tay 3 buoc (Three-way Handshake), ServerSocket tra ve mot doi tuong Socket
 *    dai dien cho kenh truyen thong rieng giua Server va Client do.
 * 4. Server su dung InputStream/OutputStream cua Socket de nhan chuoi tu Client,
 *    in ra console: 'Client says: "<noi dung>"' va gui phan hoi lai: "Received OK".
 */
public class TCPServer {

    public static final int DEFAULT_PORT = 9090;

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;
    private TCPServerListener listener;

    public TCPServer(int port) {
        this(port, null);
    }

    public TCPServer(int port, TCPServerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void setListener(TCPServerListener listener) {
        this.listener = listener;
    }

    public void start() {
        System.out.println("=============================================================================");
        System.out.println("           TCP SERVER - KIEN TRUC CLIENT / SERVER CO BAN (PORT 9090)         ");
        System.out.println("=============================================================================");

        // Dang ky shutdown hook de giai phong port khi dong ung dung
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        try {
            serverSocket = new ServerSocket(port);
            System.out.printf("-> Server da khoi dong va dang lang nghe tai cong TCP: %d\n", port);
            System.out.println("-> San sang nhan ket noi tu Client... (Nhan Ctrl+C de dung Server)\n");
            if (listener != null) {
                listener.onServerStarted(port);
            }

            while (isRunning) {
                try {
                    // Cho ket noi tu Client
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getRemoteSocketAddress().toString();
                    System.out.printf("[Server] Co ket noi moi thanh cong tu Client: %s\n", clientAddress);
                    if (listener != null) {
                        listener.onClientConnected(clientAddress);
                    }

                    // Xu ly giao tiep voi Client
                    handleClient(clientSocket);

                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("[Server] ServerSocket da dong.");
                        break;
                    }
                    System.err.println("[Server] Loi khi chap nhan ket noi: " + e.getMessage());
                    if (listener != null) {
                        listener.onLog("[Lỗi] Chấp nhận kết nối thất bại: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.printf("Khong the khoi dong TCPServer tren cong %d: %s\n", port, e.getMessage());
            if (listener != null) {
                listener.onLog(String.format("[Lỗi] Không thể khởi động Server trên cổng %d: %s", port, e.getMessage()));
            }
        } finally {
            stop();
        }
    }

    /**
     * Xu ly truyen nhan du lieu voi mot Client cu the.
     */
    private void handleClient(Socket socket) {
        String clientAddress = socket.getRemoteSocketAddress() != null ? socket.getRemoteSocketAddress().toString() : "Unknown";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                inputLine = inputLine.trim();

                // Neu Client gui tin hieu thoat
                if (inputLine.equalsIgnoreCase("exit") || inputLine.equalsIgnoreCase("quit")) {
                    System.out.println("[Server] Client gui lenh thoat ('" + inputLine + "'). Dang dong ket noi...");
                    writer.println("Server: Tam biet! Ket noi da duoc dong.");
                    break;
                }

                // In thong diep nhan duoc ra console Server theo dung dac ta yeu cau
                System.out.println("Client says: \"" + inputLine + "\"");

                // Gui phan hoi "Received OK" ve cho Client
                writer.println("Received OK");
                System.out.println("[Server] Da gui phan hoi: \"Received OK\"");

                if (listener != null) {
                    listener.onMessageReceived(clientAddress, inputLine, "Received OK");
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Loi giao tiep voi Client: " + e.getMessage());
            if (listener != null) {
                listener.onLog("[Lỗi] Giao tiếp với Client " + clientAddress + ": " + e.getMessage());
            }
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("[Server] Loi dong socket: " + e.getMessage());
            }
            System.out.println("[Server] Da dong ket noi voi Client. Tiep tuc cho Client tiep theo...\n");
            if (listener != null) {
                listener.onClientDisconnected(clientAddress);
            }
        }
    }

    public void stop() {
        if (!isRunning && (serverSocket == null || serverSocket.isClosed())) {
            return;
        }
        isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Loi khi dong ServerSocket: " + e.getMessage());
            }
        }
        if (listener != null) {
            listener.onServerStopped();
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        TCPServer server = new TCPServer(port);
        server.start();
    }
}
