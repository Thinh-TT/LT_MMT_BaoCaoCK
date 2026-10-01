package nhanh6;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Lop RMIClient: Tra cuu Registry va goi phuong thuc tu xa.
 *
 * Nguyen ly hoat dong:
 * 1. Client ket noi toi RMI Registry tai dia chi host va cong 1099.
 * 2. Client goi registry.lookup("HelloService") de tim kiem doi tuong tu xa.
 * 3. Registry tra ve mot doi tuong Stub (dai dien/proxy) implement HelloInterface.
 * 4. Client goi phuong thuc sayHello("SinhVien") tren Stub giong het nhu mot loi goi ham cuc bo (Local method call).
 * 5. Stub tu dong thuc hien Marshalling tham so, gui qua mang toi Server va nhan ket qua tra ve.
 * 6. In ket qua ra console theo dung dac ta: "Remote says: Hello, SinhVien!".
 */
public class RMIClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 1099;
    public static final String SERVICE_NAME = "HelloService";

    private final String host;
    private final int port;

    public RMIClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public RMIClient(String host, int port) {
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
     * Goi phuong thuc sayHello() qua Stub va tra ve chuoi ket qua (vi du: "Hello, SinhVien!").
     *
     * @param name ten nguoi dung truyen vao
     * @return ket qua tra ve tu Server
     * @throws Exception khi lookup loi hoac ket noi RMI gap su co
     */
    public String invokeSayHello(String name) throws Exception {
        return invokeSayHello(this.host, this.port, name);
    }

    /**
     * Goi phuong thuc sayHello() tren host va port xac dinh.
     *
     * @param host dia chi RMI Registry
     * @param port cong RMI Registry
     * @param name ten nguoi dung
     * @return ket qua tra ve tu Server
     * @throws Exception khi lookup loi hoac goi remote method that bai
     */
    public static String invokeSayHello(String host, int port, String name) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        HelloInterface stub = (HelloInterface) registry.lookup(SERVICE_NAME);
        return stub.sayHello(name);
    }

    /**
     * Thuc hien goi ham tu xa sayHello() theo dac ta chuan.
     *
     * @param host dia chi Server
     * @param port cong Registry (1099)
     * @param name ten truyen vao phuong thuc tu xa
     * @return true neu goi ham tu xa thanh cong
     */
    public static boolean callSayHello(String host, int port, String name) {
        System.out.println("=============================================================================");
        System.out.println("            RMI CLIENT - GOI PHUONG THUC TU XA (REMOTE METHOD CALL)          ");
        System.out.println("=============================================================================");
        System.out.printf("-> Dang ket noi toi RMI Registry tai %s:%d...\n", host, port);

        try {
            // 1. Ket noi toi RMI Registry
            Registry registry = LocateRegistry.getRegistry(host, port);

            // 2. Tra cuu (lookup) service tu Registry de lay Stub
            System.out.printf("-> Dang tra cuu (lookup) service '%s'...\n", SERVICE_NAME);
            HelloInterface stub = (HelloInterface) registry.lookup(SERVICE_NAME);
            System.out.println("-> Da lay duoc Stub proxy tu Registry thanh cong!");

            // 3. Goi phuong thuc tu xa sayHello() tren Stub
            System.out.printf("-> Dang goi phuong thuc tu xa sayHello(\"%s\")...\n", name);
            String response = stub.sayHello(name);

            // 4. In ket qua theo dung dac ta bao cao cuoi ky: Remote says: Hello, SinhVien!
            System.out.println("\n-----------------------------------------------------------------------------");
            System.out.println("Remote says: " + response);
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("-> Ket qua: [THANH CONG - GOI HAM TU XA RMI CHUAN XAC]");

            return true;

        } catch (Exception e) {
            System.err.println("-> Loi khi thuc hien RMI Client: " + e.getMessage());
            System.err.println("   Goi y: Hay kiem tra xem RMIServer da duoc bat tren cong " + port + " chua.");
            return false;
        }
    }

    /**
     * Chay o che do tuong tac nhap ten tu ban phim.
     */
    public static void runInteractive(String host, int port) {
        System.out.println("=============================================================================");
        System.out.println("          RMI CLIENT - CHE DO TUONG TAC BAN PHIM (PORT 1099)                 ");
        System.out.println("=============================================================================");

        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(host, port);
            HelloInterface stub = (HelloInterface) registry.lookup(SERVICE_NAME);
            System.out.println("-> Ket noi toi RMI Registry thanh cong!");

            while (true) {
                System.out.print("\n> Nhap ten de goi phuong thuc tu xa (go 'exit' de thoat): ");
                if (!scanner.hasNextLine()) break;

                String name = scanner.nextLine().trim();
                if (name.isEmpty()) continue;
                if (name.equalsIgnoreCase("exit") || name.equalsIgnoreCase("quit")) {
                    System.out.println("-> Da thoat chuong trinh RMIClient.");
                    break;
                }

                String response = stub.sayHello(name);
                System.out.println("Remote says: " + response);
            }

        } catch (Exception e) {
            System.err.println("Loi tuong tac RMIClient: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        String name = "SinhVien";

        if (args.length > 0 && args[0].equalsIgnoreCase("--interactive")) {
            if (args.length >= 2) host = args[1];
            if (args.length >= 3) {
                try { port = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
            }
            runInteractive(host, port);
        } else {
            if (args.length >= 1 && !args[0].startsWith("-")) {
                name = args[0];
            }
            if (args.length >= 2) {
                host = args[1];
            }
            if (args.length >= 3) {
                try { port = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {}
            }
            callSayHello(host, port, name);
        }
    }
}
