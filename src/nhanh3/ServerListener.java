package nhanh3;

/**
 * Interface lang nghe cac su kien tu MultiThreadedServer va ClientHandler.
 * Dung de dong bo du lieu len giao dien do hoa (ServerForm) realtime.
 */
public interface ServerListener {

    /**
     * Su kien Server xuat dong log chung.
     */
    void onLog(String message);

    /**
     * Su kien Server khoi dong thanh cong.
     */
    void onServerStarted(int port, int poolSize);

    /**
     * Su kien co Client moi ket noi den Server.
     */
    void onClientConnected(int clientId, String clientAddress, String connectTime);

    /**
     * Su kien Client gui chuoi va Server tra ve chuoi dao nguoc.
     */
    void onClientMessage(int clientId, String threadName, String receivedText, String sentText);

    /**
     * Su kien Client ngat ket noi khoi Server.
     */
    void onClientDisconnected(int clientId, String status);

    /**
     * Su kien Server dung hoan toan.
     */
    void onServerStopped();
}
