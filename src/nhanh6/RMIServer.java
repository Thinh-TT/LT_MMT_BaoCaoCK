package nhanh6;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Lop RMIServer: Khoi dong RMI Registry va dang ky Remote Object.
 *
 * Nguyen ly hoat dong:
 * 1. RMI Registry dong vai tro la mot Naming Service (Dich vu danh muc/so dien thoai),
 *    giup Client tim kiem (lookup) cac doi tuong tu xa thong qua ten chuoi (String key).
 * 2. Server goi LocateRegistry.createRegistry(1099) de khoi dong Registry tren cong mac dinh 1099.
 * 3. Server tao the hien cua HelloImpl va dang ky (rebind) vao Registry voi ten "HelloService".
 * 4. Khi dang ky, thuc chat ban sao Stub cua HelloImpl duoc luu tru trong Registry de san sang tra ve cho Client.
 */
public class RMIServer {

    public static final int DEFAULT_PORT = 1099;
    public static final String SERVICE_NAME = "HelloService";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("=============================================================================");
        System.out.println("         RMI SERVER - DANG KY DICH VU VAO RMI REGISTRY (PORT 1099)           ");
        System.out.println("=============================================================================");

        try {
            // 1. Khoi tao Registry tren cong 1099 tai Server (neu chua ton tai)
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.printf("-> RMI Registry da duoc tao va khoi dong tren cong TCP: %d\n", port);
            } catch (RemoteException e) {
                // Neu da co Registry chay truoc do, lay the hien cua no
                registry = LocateRegistry.getRegistry(port);
                System.out.printf("-> RMI Registry da san sang tren cong TCP: %d\n", port);
            }

            // 2. Khoi tao doi tuong trien khai Remote Object
            HelloImpl helloService = new HelloImpl();

            // 3. Dang ky (bind / rebind) doi tuong vao Registry voi ten "HelloService"
            registry.rebind(SERVICE_NAME, helloService);

            System.out.printf("-> Da dang ky thanh cong service '%s' vao RMI Registry.\n", SERVICE_NAME);
            System.out.println("-> Server dang hoat dong va cho Client goi phuong thuc tu xa...");
            System.out.println("-> (Nhan Ctrl+C de dung Server)\n");

            // Dang ky shutdown hook de huy dang ky khi tat server
            final Registry finalRegistry = registry;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(SERVICE_NAME);
                    System.out.println("\n-> Da huy dang ky (unbind) service '" + SERVICE_NAME + "' khoi Registry.");
                } catch (Exception ignored) {}
                System.out.println("-> RMIServer da dung thanh cong.");
            }));

            // Giu chuong trinh Server tiep tuc chay
            synchronized (RMIServer.class) {
                RMIServer.class.wait();
            }

        } catch (Exception e) {
            System.err.println("Loi khoi dong RMIServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
