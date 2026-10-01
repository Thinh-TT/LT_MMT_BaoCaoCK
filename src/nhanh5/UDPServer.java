package nhanh5;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Lop UDPServer: Minh hoa nguyen ly hoat dong cua giao thuc UDP va DatagramSocket.
 *
 * Nguyen ly hoat dong:
 * 1. Khong thiet lap ket noi truoc (Connectionless): Server khong can goi accept() nhu TCP.
 * 2. Khoi tao DatagramSocket lang nghe tren cong UDP 9191.
 * 3. Nhan du lieu duoi dang cac goi tin doc lap (DatagramPacket).
 * 4. Trich xuat thong tin dia chi IP va Port cua nguoi gui ngay tu header cua DatagramPacket.
 * 5. Neu nhan duoc chuoi "Ping", tao goi tin DatagramPacket chua chuoi "Pong" va gui nguoc lai.
 */
public class UDPServer {

    public static final int DEFAULT_PORT = 9191;
    public static final int BUFFER_SIZE = 1024;

    private final int port;
    private DatagramSocket socket;
    private volatile boolean isRunning = true;

    public UDPServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("=============================================================================");
        System.out.println("            UDP SERVER - TRUYEN NHAN KHONG KET NOI (PORT 9191)               ");
        System.out.println("=============================================================================");

        // Dang ky shutdown hook de giai phong Socket khi dong ung dung
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        try {
            socket = new DatagramSocket(port);
            System.out.printf("-> UDPServer da khoi dong tren cong UDP: %d\n", port);
            System.out.println("-> San sang nhan DatagramPacket tu Client... (Nhan Ctrl+C de dung)\n");

            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (isRunning) {
                try {
                    // Khoi tao goi tin de hung du lieu den
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    
                    // Phuong thuc receive() se block cho den khi co mot goi tin UDP duoc gui den cong 9191
                    socket.receive(receivePacket);

                    // Trich xuat thong tin goi tin
                    String receivedMessage = new String(
                            receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();
                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    System.out.printf("[UDPServer] Nhan goi tin tu [%s:%d]: \"%s\"\n",
                            clientAddress.getHostAddress(), clientPort, receivedMessage);

                    // Xu ly logic Ping -> Pong
                    String responseMessage;
                    if (receivedMessage.equalsIgnoreCase("Ping")) {
                        responseMessage = "Pong";
                    } else if (receivedMessage.equalsIgnoreCase("exit") || receivedMessage.equalsIgnoreCase("quit")) {
                        responseMessage = "Tam biet!";
                        System.out.printf("[UDPServer] Client [%s:%d] thong bao ket thuc.\n",
                                clientAddress.getHostAddress(), clientPort);
                    } else {
                        responseMessage = "Echo: " + receivedMessage;
                    }

                    // Dong goi phan hoi vao mot DatagramPacket moi gui ve cho Client
                    byte[] sendData = responseMessage.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket sendPacket = new DatagramPacket(
                            sendData, sendData.length, clientAddress, clientPort);

                    socket.send(sendPacket);
                    System.out.printf("[UDPServer] Da gui phan hoi toi [%s:%d]: \"%s\"\n\n",
                            clientAddress.getHostAddress(), clientPort, responseMessage);

                } catch (SocketException e) {
                    if (!isRunning) {
                        System.out.println("[UDPServer] Socket da duoc dong.");
                        break;
                    }
                    System.err.println("[UDPServer] Loi Socket: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("[UDPServer] Loi I/O khi nhan/gui goi tin: " + e.getMessage());
                }
            }

        } catch (SocketException e) {
            System.err.printf("Khong the khoi tao DatagramSocket tren cong %d: %s\n", port, e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("-> UDPServer da dung va dong Socket thanh cong.");
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        UDPServer server = new UDPServer(port);
        server.start();
    }
}
