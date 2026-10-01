package nhanh3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server TCP da luong (Multithreaded Server) su dung mo hinh ThreadPool.
 *
 * Dac diem kien truc:
 * 1. ServerSocket lang nghe ket noi TCP tren cong 8080 (hoac cong tuy chon).
 * 2. Thay vi tao thread vo han (Thread-per-client) de dan toi tran bo nho (OOM),
 *    Server su dung ExecutorService (FixedThreadPool) de gioi han so luong thread worker dong thoi.
 * 3. Khi co ket noi den tu accept(), ket noi duoc dong goi thanh ClientHandler va dua vao hang doi (Queue) cua ThreadPool.
 * 4. Thread worker trong pool se lay yeu cau ra xu ly doc lap, dam bao phuc vu nhieu Client song song ma khong can thiep vao nhau.
 */
public class MultiThreadedServer {

    public static final int DEFAULT_PORT = 8080;
    public static final int THREAD_POOL_SIZE = 10;

    private final int port;
    private final int poolSize;
    private final AtomicInteger clientCounter = new AtomicInteger(0);
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean isRunning = true;
    private ServerListener listener;

    public MultiThreadedServer(int port, int poolSize) {
        this(port, poolSize, null);
    }

    public MultiThreadedServer(int port, int poolSize, ServerListener listener) {
        this.port = port;
        this.poolSize = poolSize;
        this.listener = listener;
    }

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    public void start() {
        System.out.println("=============================================================================");
        System.out.println("        SERVER TCP DA LUONG (MULTITHREADED SERVER VOI THREADPOOL)           ");
        System.out.println("=============================================================================");

        threadPool = Executors.newFixedThreadPool(poolSize);

        // Dang ky shutdown hook de giai phong tai nguyen khi Server bi tat
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        try {
            serverSocket = new ServerSocket(port);
            System.out.printf("-> Server da khoi dong thanh cong tren cong TCP: %d\n", port);
            System.out.printf("-> ThreadPool khoi tao: %d luong worker san sang xu ly dong thoi\n", poolSize);
            System.out.println("-> Dang cho Client ket noi den...\n");

            if (listener != null) {
                listener.onServerStarted(port, poolSize);
            }

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientCounter.incrementAndGet();
                    String clientAddr = clientSocket.getRemoteSocketAddress().toString();
                    String connectTime = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

                    System.out.printf("[Main-Server] Chap nhan ket noi moi #%d tu: %s. Dang chuyen vao ThreadPool...\n",
                            clientId, clientAddr);

                    if (listener != null) {
                        listener.onClientConnected(clientId, clientAddr, connectTime);
                    }

                    // Chuyen giao viec xu ly cho ThreadPool
                    threadPool.submit(new ClientHandler(clientSocket, clientId, listener));

                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("[Main-Server] ServerSocket da duoc dong.");
                        break;
                    }
                    System.err.println("[Main-Server] Loi khi accept ket noi: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.printf("Khong the khoi dong Server tren cong %d: %s\n", port, e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        System.out.println("\n-> Dang dung Server va dong ThreadPool...");
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Loi khi dong ServerSocket: " + e.getMessage());
            }
        }
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        if (listener != null) {
            listener.onServerStopped();
        }
        System.out.println("-> Server da dung hoan toan.");
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        int poolSize = THREAD_POOL_SIZE;

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 2) {
            try {
                poolSize = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        MultiThreadedServer server = new MultiThreadedServer(port, poolSize);
        server.start();
    }
}
