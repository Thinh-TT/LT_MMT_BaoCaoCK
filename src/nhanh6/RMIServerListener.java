package nhanh6;

/**
 * Interface lang nghe cac su kien tu RMI Server va Remote Object (HelloImpl).
 * Ho tro cap nhat giao dien do hoa (ServerForm) theo thoi gian thuc.
 */
public interface RMIServerListener {

    /**
     * Su kien Server xuat thong diep log chung.
     */
    void onLog(String message);

    /**
     * Su kien RMI Registry khoi dong thanh cong tai cong port chi dinh.
     */
    void onRegistryStarted(int port);

    /**
     * Su kien dang ky Remote Object vao Registry voi ten serviceName.
     */
    void onServiceBound(String serviceName);

    /**
     * Su kien Client thuc hien loi goi ham tu xa (Remote Method Call) sayHello().
     *
     * @param clientHost dia chi host cua Client (neu xac dinh duoc)
     * @param paramName tham so name duoc truyen tu Client
     * @param result ket qua tra ve sau khi HelloImpl thuc thi
     */
    void onRemoteCall(String clientHost, String paramName, String result);

    /**
     * Su kien huy dang ky service khoi Registry.
     */
    void onServiceUnbound(String serviceName);

    /**
     * Su kien RMI Server va Registry dung hoan toan.
     */
    void onServerStopped();
}
