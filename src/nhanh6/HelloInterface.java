package nhanh6;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Giao dien tu xa (Remote Interface) cho kien truc RMI.
 *
 * Nguyen ly hoat dong:
 * 1. Phai ke thua (extends) tu java.rmi.Remote de danh dau day la mot Remote Object.
 * 2. Tat ca cac phuong thuc duoc khai bao trong interface nay deu phai nem (throws)
 *    ngoai le java.rmi.RemoteException de xu ly cac loi truyen thong mang (Network Failure,
 *    Connection Loss, Serialization Error,...).
 * 3. Client se tuong tac truc tiep voi Interface nay thong qua Stub (Proxy Object)
 *    ma khong can biet chi tiet trien khai phia Server.
 */
public interface HelloInterface extends Remote {

    /**
     * Phuong thuc goi tu xa: Nhan ten va tra ve loi chao.
     *
     * @param name ten cua nguoi goi tu Client
     * @return chuoi chao mung duoc tao tu Server
     * @throws RemoteException loi lien quan toi truyen thong tu xa RMI
     */
    String sayHello(String name) throws RemoteException;
}
