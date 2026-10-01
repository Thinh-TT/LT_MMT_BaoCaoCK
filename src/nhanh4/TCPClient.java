package nhanh4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Lop TCPClient: Client ket noi toi TCPServer qua Socket TCP.
 *
 * Nguyen ly hoat dong:
 * 1. Khoi tao doi tuong Socket("localhost", 9090).
 * 2. Gui yeu cau thiet lap ket noi toi Server (qua trinh bat tay 3 buoc TCP dien ra tu dong).
 * 3. Gui chuoi ky tu nguoi dung nhap tu ban phim qua OutputStream.
 * 4. Nhan va in ra phan hoi tu Server: 'Server: Received OK'.
 * 5. Dong ket noi bang cach gui 'exit' hoac dong socket.
 */
public class TCPClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 9090;

    /**
     * Chay Client o che do tuong tac go ban phim truc tiep.
     */
    public static void runInteractive(String host, int port) {
        System.out.println("=============================================================================");
        System.out.println("          TCP CLIENT - CHE DO TUONG TAC BAN PHIM (PORT 9090)                 ");
        System.out.println("=============================================================================");
        System.out.printf("-> Dang ket noi toi TCPServer tai %s:%d...\n", host, port);

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("-> Ket noi toi Server thanh cong!");
            System.out.println("-> Nhap chuoi bat ky de gui den Server (go 'exit' de ngat ket noi):\n");

            while (true) {
                System.out.print("> Nhap tin nhan: ");
                if (!scanner.hasNextLine()) break;

                String message = scanner.nextLine();
                if (message == null || message.trim().isEmpty()) continue;

                // Gui chuoi den Server
                writer.println(message);

                // Nhan phan hoi tu Server
                String response = reader.readLine();
                if (response == null) {
                    System.out.println("[Client] Server da dong ket noi.");
                    break;
                }

                // In phan hoi theo dung format yeu cau: Server: Received OK
                System.out.println("Server: " + response);

                if (message.trim().equalsIgnoreCase("exit") || message.trim().equalsIgnoreCase("quit")) {
                    System.out.println("-> Da thoat chuong trinh TCPClient.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Loi ket noi TCPClient: " + e.getMessage());
            System.err.println("Goi y: Hay dam bao TCPServer da duoc khoi dong truoc khi chay TCPClient.");
        }
    }

    /**
     * Chay Client o che do kiem thu tu dong (gui mot chuoi va kiem tra phan hoi Received OK).
     *
     * @param host dia chi server
     * @param port cong server
     * @param message chuoi can gui
     * @return true neu server phan hoi 'Received OK'
     */
    public static boolean runAutomatedTest(String host, int port, String message) {
        System.out.println("=============================================================================");
        System.out.println("            TCP CLIENT - CHE DO KIEM THU TU DONG (AUTOMATED TEST)            ");
        System.out.println("=============================================================================");
        System.out.printf("-> Dang ket noi toi %s:%d...\n", host, port);

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("-> Ket noi thanh cong!");
            System.out.printf("-> Gui den Server: \"%s\"\n", message);
            writer.println(message);

            String response = reader.readLine();
            System.out.println("Server: " + response);

            boolean isSuccess = "Received OK".equalsIgnoreCase(response != null ? response.trim() : "");
            System.out.printf("-> Ket qua kiem thu: %s\n", (isSuccess ? "[THANH CONG - DUNG DAC TA]" : "[THAT BAI]"));

            // Gui lenh thoat de dong ket noi an toan
            writer.println("exit");
            reader.readLine();

            return isSuccess;

        } catch (IOException e) {
            System.err.println("Loi kiem thu TCPClient: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 0 && args[0].equals("--auto")) {
            String testMessage = (args.length > 1) ? args[1] : "Xin chao";
            runAutomatedTest(host, port, testMessage);
        } else {
            if (args.length >= 1) {
                host = args[0];
            }
            if (args.length >= 2) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }
            runInteractive(host, port);
        }
    }
}
