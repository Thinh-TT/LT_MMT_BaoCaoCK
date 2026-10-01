package nhanh4;

/**
 * Interface lang nghe cac su kien tu TCPServer.
 * Ho tro cap nhat giao dien do hoa (ServerForm) theo thoi gian thuc.
 */
public interface TCPServerListener {

    /**
     * Su kien Server xuat thong diep log chung.
     */
    void onLog(String message);

    /**
     * Su kien Server khoi dong thanh cong va dang lang nghe tai cong port.
     */
    void onServerStarted(int port);

    /**
     * Su kien co mot Client ket noi thanh cong den Server.
     */
    void onClientConnected(String clientAddress);

    /**
     * Su kien Server nhan thong diep tu Client va gui phan hoi lai.
     */
    void onMessageReceived(String clientAddress, String receivedMessage, String replyMessage);

    /**
     * Su kien Client dong hoac ngat ket noi khoi Server.
     */
    void onClientDisconnected(String clientAddress);

    /**
     * Su kien Server dung hoan toan va dong ServerSocket.
     */
    void onServerStopped();
}
