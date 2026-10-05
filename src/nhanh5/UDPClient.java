package nhanh5;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Lop UDPClient: Gui goi tin DatagramPacket qua DatagramSocket toi UDPServer.
 *
 * Nguyen ly hoat dong:
 * 1. Khoi tao DatagramSocket khong gan cong co dinh (he dieu hanh tu cap phat cong ngau nhien).
 * 2. Thiet lap thoi gian cho toi da (Timeout, vi du 3000ms) vi UDP khong dam bao tin cay,
 *    goi tin co the bi mat tren duong truyen ma khong co ACK.
 * 3. Dong goi chuoi "Ping" thanh DatagramPacket kem dia chi IP va cong 9191 cua Server.
 * 4. Nhan goi tin phan hoi va in ra man hinh: "Sent: Ping -> Received: Pong".
 */
public class UDPClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 9191;
    public static final int BUFFER_SIZE = 1024;
    public static final int TIMEOUT_MS = 3000;

    private final String host;
    private final int port;

    public UDPClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public UDPClient(String host, int port) {
        this.host = (host != null && !host.trim().isEmpty()) ? host.trim() : DEFAULT_HOST;
        this.port = (port > 0) ? port : DEFAULT_PORT;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * Gui mot DatagramPacket chua thong diep va cho nhan phan hoi tu UDPServer.
     * Su dung host va port cua instance hien tai.
     *
     * @param message chuoi can gui (vi du: "Ping")
     * @return chuoi phan hoi tu Server (vi du: "Pong")
     * @throws IOException khi gap loi mang hoac het thoi gian cho (Timeout)
     */
    public String sendAndReceive(String message) throws IOException {
        return sendAndReceive(this.host, this.port, message, TIMEOUT_MS);
    }

    /**
     * Gui thong diep qua UDP toi host va port xac dinh, nhan phan hoi voi timeout mac dinh 3000ms.
     *
     * @param host dia chi may chu UDP
     * @param port cong may chu UDP
     * @param message chuoi can gui
     * @return chuoi phan hoi tu Server
     * @throws IOException khi gap loi truyen nhan hoac timeout
     */
    public static String sendAndReceive(String host, int port, String message) throws IOException {
        return sendAndReceive(host, port, message, TIMEOUT_MS);
    }

    /**
     * Gui thong diep qua UDP va cho nhan phan hoi voi timeout tuy chinh.
     *
     * @param host dia chi may chu UDP
     * @param port cong may chu UDP
     * @param message chuoi can gui
     * @param timeoutMs thoi gian cho toi da (ms)
     * @return chuoi phan hoi tu Server
     * @throws IOException khi gap loi truyen nhan hoac timeout
     */
    public static String sendAndReceive(String host, int port, String message, int timeoutMs) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeoutMs);
            InetAddress serverAddress = InetAddress.getByName(host);

            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            return new String(
                    receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
        }
    }

    /**
     * Thuc hien chu trinh UDP Ping-Pong theo dung dac ta bao cao cuoi ky.
     *
     * @param host dia chi UDPServer
     * @param port cong UDPServer
     * @return true neu gui "Ping" va nhan duoc "Pong" thanh cong
     */
    public static boolean sendPingPong(String host, int port) {
        System.out.println("=============================================================================");
        System.out.println("            UDP CLIENT - DEMO PING PONG (DATAGRAM SOCKET)                    ");
        System.out.println("=============================================================================");
        System.out.printf("-> Dang khoi tao goi tin UDP gui toi %s:%d...\n", host, port);

        try (DatagramSocket socket = new DatagramSocket()) {
            // Thiet lap Timeout de tranh bi treo neu goi tin bi mat
            socket.setSoTimeout(TIMEOUT_MS);

            InetAddress serverAddress = InetAddress.getByName(host);
            String message = "Ping";

            // 1. Dong goi du lieu "Ping" vao DatagramPacket
            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);

            // 2. Gui goi tin
            socket.send(sendPacket);

            // 3. Cho nhan goi tin phan hoi "Pong" tu Server
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            socket.receive(receivePacket);

            // 4. Trich xuat thong diep
            String response = new String(
                    receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();

            // In ket qua theo dung dac ta: Sent: Ping -> Received: Pong
            System.out.printf("\nSent: %s -> Received: %s\n\n", message, response);

            boolean isSuccess = "Pong".equalsIgnoreCase(response);
            if (isSuccess) {
                System.out.println("-> Ket qua: [THANH CONG - DUNG CHUAN DAC TA UDP PING-PONG]");
            } else {
                System.out.println("-> Ket qua: [PHAN HOI KHAC DONG BO: " + response + "]");
            }

            return isSuccess;

        } catch (SocketTimeoutException e) {
            System.err.println("-> Loi: Het thoi gian cho (Timeout " + TIMEOUT_MS + "ms)! Goi tin co the da bi mat.");
            System.err.println("   Goi y: Hay kiem tra UDPServer da khoi dong tren cong " + port + " chua.");
            return false;
        } catch (IOException e) {
            System.err.println("-> Loi truyen nhan UDP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Chay Client o che do tuong tac go ban phim.
     */
    public static void runInteractive(String host, int port) {
        System.out.println("=============================================================================");
        System.out.println("          UDP CLIENT - CHE DO TUONG TAC BAN PHIM (PORT 9191)                 ");
        System.out.println("=============================================================================");
        System.out.printf("-> Muc tieu: %s:%d\n", host, port);
        System.out.println("-> Nhap chuoi bat ky de gui qua UDP (go 'Ping' hoac 'exit' de thoat):\n");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {

            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(host);

            while (true) {
                System.out.print("> Nhap tin nhan: ");
                if (!scanner.hasNextLine()) break;

                String message = scanner.nextLine();
                if (message == null || message.trim().isEmpty()) continue;

                byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                try {
                    socket.receive(receivePacket);
                    String response = new String(
                            receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();

                    System.out.printf("Sent: %s -> Received: %s\n", message, response);

                    if (message.trim().equalsIgnoreCase("exit") || message.trim().equalsIgnoreCase("quit")) {
                        System.out.println("-> Da thoat chuong trinh UDPClient.");
                        break;
                    }

                } catch (SocketTimeoutException e) {
                    System.err.println("-> Khong nhan duoc phan hoi (Timeout). Goi tin UDP co the da bi rot.");
                }
            }

        } catch (IOException e) {
            System.err.println("Loi UDPClient: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 0 && args[0].equalsIgnoreCase("--pingpong")) {
            if (args.length >= 2) {
                host = args[1];
            }
            if (args.length >= 3) {
                try {
                    port = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {}
            }
            sendPingPong(host, port);
        } else {
            if (args.length >= 1 && !args[0].startsWith("-")) {
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
