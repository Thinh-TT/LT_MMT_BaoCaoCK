package nhanh6;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Lop trien khai phia Server (Remote Object Implementation).
 *
 * Nguyen ly hoat dong:
 * 1. Ke thua UnicastRemoteObject de co the xuat (export) doi tuong tu xa qua giao thuc JRMP (Java Remote Method Protocol)
 *    va lang nghe cac ket noi TCP den tren mot cong an danh (ephemeral port).
 * 2. Implements HelloInterface de dinh nghia hanh vi thuc te cua phuong thuc sayHello().
 * 3. Khi Client thuc hien loi goi ham tu xa:
 *    - Phia Client: Stub thuc hien Marshalling (tuan tu hoa tham so 'name') gui qua mang.
 *    - Phia Server: Skeleton / Dispatcher nhan du lieu, thuc hien Unmarshalling va goi HelloImpl.sayHello().
 *    - Server thuc thi logic va tra ve ket qua qua Skeleton ve Stub cho Client.
 */
public class HelloImpl extends UnicastRemoteObject implements HelloInterface {

    private static final long serialVersionUID = 1L;

    public HelloImpl() throws RemoteException {
        super();
    }

    @Override
    public String sayHello(String name) throws RemoteException {
        // Log ro thu tu Stub -> Skeleton -> Server theo dung yeu cau dac ta bao cao cuoi ky
        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("[RMI Server Pipeline] Co loi goi phuong thuc tu xa den sayHello():");
        System.out.println("-> [Buoc 1: Stub]     Client-side Stub thuc hien Marshalling tham so: \"" + name + "\"");
        System.out.println("-> [Buoc 2: Skeleton] Server-side Skeleton nhan du lieu tu mang va Unmarshalling thanh cong");
        System.out.println("-> [Buoc 3: Server]   HelloImpl.sayHello(\"" + name + "\") dang thuc thi tren Server JVM");
        System.out.println("==> Thu tu xu ly: Stub -> Skeleton -> HelloImpl.sayHello()");
        System.out.println("-----------------------------------------------------------------------------");

        return "Hello, " + name + "!";
    }
}
