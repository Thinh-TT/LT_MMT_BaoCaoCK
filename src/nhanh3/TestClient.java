package nhanh3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client TCP ket noi toi MultiThreadedServer de gui chuoi va nhan chuoi dao nguoc.
 *
 * Ho tro 2 che do:
 * 1. Che do tuong tac (Interactive Mode): Nhap chuoi truc tiep tu ban phim tren terminal.
 * 2. Che do gia lap dong thoi (Concurrent Simulation Mode): Tu dong tao >= 3 Client chay song song
 *    de kiem chung Server phuc vu dong thoi khong bi lan lon ket qua giua cac Client.
 */
public class TestClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;

    /**
     * Chay Client o che do tuong tac qua console.
     */
    public static void runInteractive(String host, int port) {
        System.out.println("==================================================");
        System.out.println("     TCP CLIENT - CHE DO TUONG TAC BAN PHIM       ");
        System.out.println("==================================================");
        System.out.printf("-> Dang ket noi toi Server tai %s:%d...\n", host, port);

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("-> Ket noi thanh cong!");

            // Doc loi chao tu Server
            String serverGreeting = reader.readLine();
            System.out.println("[Phan hoi] " + serverGreeting);

            System.out.println("-> Nhap chuoi bat ky de dao nguoc (go 'exit' de thoat):");

            while (true) {
                System.out.print("> Nhap chuoi: ");
                if (!scanner.hasNextLine()) break;

                String line = scanner.nextLine();
                if (line == null || line.trim().isEmpty()) continue;

                // Gui chuoi den Server
                writer.println(line);

                // Nhan phan hoi dao nguoc tu Server
                String response = reader.readLine();
                if (response == null) {
                    System.out.println("-> Server da dong ket noi.");
                    break;
                }

                System.out.println("-> Ket qua tu Server: " + response);

                if (line.trim().equalsIgnoreCase("exit") || line.trim().equalsIgnoreCase("quit")) {
                    System.out.println("-> Da thoat chuong trinh Client.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Loi ket noi Client: " + e.getMessage());
            System.err.println("Goi y: Hay kiem tra xem MultiThreadedServer da duoc bat truoc do chua.");
        }
    }

    /**
     * Chay gia lap nhieu Client song song de kiem thu Server da luong tu dong.
     *
     * @param host       dia chi server
     * @param port       cong server
     * @param numClients so luong client song song (it nhat 3 client)
     */
    public static void runConcurrentSimulation(String host, int port, int numClients) {
        System.out.println("=============================================================================");
        System.out.printf("   GIA LAP %d CLIENT KET NOI SONG SONG TOI SERVER (%s:%d)   \n", numClients, host, port);
        System.out.println("=============================================================================");

        String[] testStrings = {
                "hello world",
                "lap trinh mang can ban",
                "multithreaded server",
                "socket tcp 8080",
                "he thong phan tan"
        };

        ExecutorService clientPool = Executors.newFixedThreadPool(numClients);
        CountDownLatch latch = new CountDownLatch(numClients);

        long startSimulationTime = System.nanoTime();

        for (int i = 0; i < numClients; i++) {
            final int index = i;
            final String messageToSend = testStrings[index % testStrings.length];

            clientPool.submit(() -> {
                try (Socket socket = new Socket(host, port);
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter writer = new PrintWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    // Doc loi chao server
                    String greeting = reader.readLine();

                    // Gui chuoi can dao nguoc
                    writer.println(messageToSend);
                    String reversedResponse = reader.readLine();

                    // Gui lenh thoat
                    writer.println("exit");
                    String exitResponse = reader.readLine();

                    // Kiem tra tinh dung dan cua chuoi dao nguoc
                    String expected = new StringBuilder(messageToSend).reverse().toString();
                    boolean isCorrect = expected.equals(reversedResponse);

                    System.out.printf("[SimulatedClient-%d] Gui: \"%s\" | Nhan: \"%s\" | Ket qua: %s\n",
                            index + 1, messageToSend, reversedResponse, (isCorrect ? "[DUNG]" : "[SAI]"));

                } catch (IOException e) {
                    System.err.printf("[SimulatedClient-%d] Loi ket noi: %s\n", index + 1, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            clientPool.shutdown();
        }

        double totalDurationMs = (System.nanoTime() - startSimulationTime) / 1_000_000.0;
        System.out.println("=============================================================================");
        System.out.printf("-> Hoan tat gia lap %d Client song song trong %.2f ms!\n", numClients, totalDurationMs);
        System.out.println("-> Tat ca Client deu nhan duoc chuoi dao nguoc chinh xac tu ThreadPool cua Server.");
        System.out.println("=============================================================================");
    }

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if (args.length > 0 && args[0].equals("--simulate")) {
            int numClients = 3;
            if (args.length > 1) {
                try {
                    numClients = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {}
            }
            runConcurrentSimulation(host, port, numClients);
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
