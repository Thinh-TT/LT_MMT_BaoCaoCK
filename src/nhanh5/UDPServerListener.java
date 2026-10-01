package nhanh5;

/**
 * Interface lang nghe cac su kien tu UDPServer.
 * Ho tro cap nhat giao dien do hoa (ServerForm) realtime.
 */
public interface UDPServerListener {

    /**
     * Su kien Server xuat thong diep log chung.
     */
    void onLog(String message);

    /**
     * Su kien Server khoi dong thanh cong va dang lang nghe tai cong port UDP.
     */
    void onServerStarted(int port);

    /**
     * Su kien Server nhan duoc goi tin DatagramPacket tu Client va gui goi tin phan hoi.
     *
     * @param clientAddress dia chi IP cua Client gui den
     * @param clientPort cong nguon cua Client
     * @param receivedMessage noi dung giai ma tu DatagramPacket nhan
     * @param responseMessage noi dung goi tin dong goi phan hoi lai
     */
    void onPacketReceived(String clientAddress, int clientPort, String receivedMessage, String responseMessage);

    /**
     * Su kien Server dung hoan toan va dong DatagramSocket.
     */
    void onServerStopped();
}
