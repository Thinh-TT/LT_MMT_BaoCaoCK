package nhanh3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Lop xu ly giao tiep voi tung Client tren mot Thread rieng biet.
 * Implements Runnable de co the chay trong ThreadPool (ExecutorService).
 *
 * Chuc nang:
 * 1. Nhan Socket tu MultiThreadedServer.
 * 2. Doc chuoi ky tu do Client gui den qua BufferedReader.
 * 3. Dao nguoc chuoi bang StringBuilder.reverse().
 * 4. Gui chuoi dao nguoc ve cho Client qua PrintWriter.
 * 5. In log thong tin chi tiet (Ten Thread, Client ID, noi dung truyen nhan).
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final int clientId;
    private final ServerListener listener;

    public ClientHandler(Socket clientSocket, int clientId) {
        this(clientSocket, clientId, null);
    }

    public ClientHandler(Socket clientSocket, int clientId, ServerListener listener) {
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.listener = listener;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        System.out.printf("[%s] [Client-%d] Bat dau phuc vu ket noi tu: %s\n",
                threadName, clientId, clientAddress);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Gui loi chao mung va huong dan den Client
            writer.println("SERVER: Xin chao Client-" + clientId + "! Gui chuoi de dao nguoc, go 'exit' de thoat.");

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                inputLine = inputLine.trim();

                // Kiem tra tin hieu ngat ket noi
                if (inputLine.equalsIgnoreCase("exit") || inputLine.equalsIgnoreCase("quit")) {
                    System.out.printf("[%s] [Client-%d] Nhan lenh ket thuc ('%s'). Dang dong ket noi...\n",
                            threadName, clientId, inputLine);
                    writer.println("SERVER: Tam biet! Ket noi da duoc dong.");
                    break;
                }

                // Xu ly dao nguoc chuoi
                String reversedString = new StringBuilder(inputLine).reverse().toString();

                // In log tren console Server
                System.out.printf("[%s] [Client-%d] Nhan: \"%s\" -> Gui lai: \"%s\"\n",
                        threadName, clientId, inputLine, reversedString);

                if (listener != null) {
                    listener.onClientMessage(clientId, threadName, inputLine, reversedString);
                }

                // Gui chuoi dao nguoc ve cho Client
                writer.println(reversedString);
            }

        } catch (IOException e) {
            System.err.printf("[%s] [Client-%d] Loi giao tiep I/O: %s\n",
                    threadName, clientId, e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.printf("[%s] [Client-%d] Loi khi dong socket: %s\n",
                        threadName, clientId, e.getMessage());
            }
            if (listener != null) {
                listener.onClientDisconnected(clientId, "Done");
            }
            System.out.printf("[%s] [Client-%d] Da ket thuc phien lam viec va giai phong luong.\n",
                    threadName, clientId);
        }
    }
}
