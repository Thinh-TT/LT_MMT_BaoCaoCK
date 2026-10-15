# CẨM NANG VẤN ĐÁP CHUYÊN SÂU – NHÁNH 6
## CHỦ ĐỀ: KIẾN TRÚC RMI (REMOTE METHOD INVOCATION) – VAI TRÒ CỦA STUB, SKELETON VÀ REGISTRY

> **Package:** `nhanh6`  
> **Các lớp chính:** `HelloInterface.java`, `HelloImpl.java`, `RMIServer.java`, `RMIClient.java`, `RMIServerListener.java`, `ServerForm.java`, `ClientForm.java`  
> **Cổng dịch vụ:** RMI Registry TCP `1099`  
> **Mục tiêu:** Nắm vững kiến trúc tính toán phân tán trong Java, vai trò của Stub, Skeleton/Dispatcher và Registry, cơ chế Marshalling / Unmarshalling, giao thức JRMP và trả lời xuất sắc các câu hỏi vấn đáp.

---

## 1. TỔNG QUAN LÝ THUYẾT CỐT LÕI

### 1.1. Java RMI là gì? Tại sao phải dùng RMI thay vì Socket?
- **Java RMI (Remote Method Invocation - Triệu gọi phương thức từ xa):** Là một cơ chế hướng đối tượng phân tán cấp cao (Distributed Object Computing) của nền tảng Java.
- **Sự khác biệt căn bản so với Socket truyền thống (Nhánh 4):**
  - Trong lập trình Socket TCP thuần, lập trình viên phải làm việc ở mức **Byte thô hoặc Chuỗi văn bản thô**. Để truyền một đối tượng hoặc gọi một chức năng, ta phải tự thiết kế giao thức (Protocol), tự ghép chuỗi, tự phân tích cú pháp (Parsing), kiểm tra định dạng rất dễ sai sót.
  - Trong RMI, lập trình viên có thể **triệu gọi một phương thức của một đối tượng đang nằm trên một máy tính khác (JVM khác) một cách tự nhiên y hệt như đang gọi hàm của một đối tượng cục bộ trong cùng một bộ nhớ RAM**:
    ```java
    // Trông như gọi hàm cục bộ, nhưng thực chất hàm đang chạy ở máy chủ cách xa hàng nghìn km!
    String greeting = stub.sayHello("SinhVien");
    ```

### 1.2. Kiến trúc 3 Tầng và Thứ tự Xử lý (Stub – Skeleton – Server)

```
+-----------------------------------------------------------------------------------------------+
|                                    KIẾN TRÚC JAVA RMI                                         |
+-----------------------------------------------------------------------------------------------+

     [ CLIENT JVM ]                                                 [ SERVER JVM ]
            |                                                              |
     +--------------+                                               +--------------+
     |  RMIClient   |                                               |  RMIServer   |
     +--------------+                                               +--------------+
            |                                                              |
            | 1. registry.lookup("HelloService")                           | Đăng ký service
            v                                                              v
     +--------------+ <------- Lấy Stub về máy Client (Port 1099) ------- +--------------+
     |     STUB     |                                                     | RMI REGISTRY |
     | (ClientProxy)|                                                     | (Port 1099)  |
     +--------------+                                                     +--------------+
            |
            | 2. Gọi stub.sayHello("SinhVien")
            v
     +--------------+
     | MARSHALLING  | (Tuần tự hóa tham số thành luồng byte nhị phân)
     +--------------+
            |
            | 3. Gửi qua mạng (Giao thức JRMP / TCP)
            v
     +--------------------------------------------------------------------+
     |                         MẠNG INTERNET / LAN                        |
     +--------------------------------------------------------------------+
            |
            | 4. Tiếp nhận luồng byte
            v
     +-------------------+
     | SKELETON /        | (Giải mã luồng byte thành kiểu đối tượng Java)
     | DISPATCHER        |
     +-------------------+
            |
            | 5. Unmarshalling & Chuyển lời gọi
            v
     +-------------------+
     |    HelloImpl      | ===> THỰC THI LOGIC: HelloImpl.sayHello("SinhVien")
     |  (Remote Object)  | ===> Sinh kết quả: "Hello, SinhVien!"
     +-------------------+
            |
            | 6. Marshalling kết quả và gửi ngược lại qua mạng
            v
     [ SKELETON ] ------------ Giao thức JRMP ------------> [ STUB ]
                                                               |
                                                               v
                                                    Client nhận kết quả in ra:
                                                "Remote says: Hello, SinhVien!"
```

### 1.3. Vai trò Cốt lõi của 3 Thành phần

1. **Stub (Client Proxy - Đại sứ phía Client):**
   - Nằm trên máy Client, triển khai cùng một `Remote Interface` (`HelloInterface`) với đối tượng Server.
   - Khi Client gọi hàm, Stub chặn lời gọi lại, thực hiện **Marshalling** (đóng gói tên phương thức và các tham số thành luồng byte), mở kết nối Socket TCP ẩn tới Server và gửi gói tin đi.
   - Nhận kết quả trả về từ Server, giải mã và trả lại cho Client như một hàm bình thường.
2. **Skeleton / Dispatcher (Bộ điều phối phía Server):**
   - Nằm trên máy Server.
   - Tiếp nhận luồng byte từ Stub gửi tới, thực hiện **Unmarshalling** (giải mã tham số).
   - Gọi phương thức tương ứng trên đối tượng thực tế `HelloImpl`.
   - Lấy giá trị trả về (hoặc Exception nếu có), đóng gói lại và gửi trả về cho Stub.
   *(Lưu ý: Từ Java 5 trở đi, cơ chế Skeleton tĩnh bằng `rmic` đã được thay thế bằng Dynamic Proxy / Dispatcher tích hợp sẵn trong JVM, nhưng về mặt nguyên lý kiến trúc vẫn giữ đúng vai trò của Skeleton).*
3. **RMI Registry (Dịch vụ Danh bạ Naming):**
   - Là một tiến trình dịch vụ tra cứu (Naming Service) chạy mặc định trên cổng TCP **`1099`**.
   - **Phía Server:** Dùng lệnh `registry.rebind("HelloService", helloImpl)` để đăng ký đối tượng vào danh bạ kèm một tên định danh chuỗi.
   - **Phía Client:** Dùng lệnh `registry.lookup("HelloService")` để tìm kiếm và tải bản sao Stub về Client JVM.

---

## 2. MỔ XẺ MÃ NGUỒN (CODEBASE WALKTHROUGH)

### 2.1. Giao diện `HelloInterface.java`
```java
public interface HelloInterface extends Remote {
    String sayHello(String name) throws RemoteException;
}
```
- **Kế thừa `java.rmi.Remote`:** Đây là một **Marker Interface** (giao diện đánh dấu) để thông báo cho trình biên dịch Java và RMI runtime biết rằng phương thức trong interface này có thể được gọi từ một máy ảo khác.
- **Bắt buộc `throws RemoteException`:** Mọi cuộc gọi qua mạng đều tiềm ẩn nguy cơ đứt cáp, mất mạng, timeout, hoặc lỗi tuần tự hóa. Java ép buộc lập trình viên phải bắt ngoại lệ `RemoteException` để đảm bảo hệ thống phân tán có cơ chế phòng vệ lỗi mạng.

### 2.2. Lớp Triển khai `HelloImpl.java`
```java
public class HelloImpl extends UnicastRemoteObject implements HelloInterface {
    public HelloImpl() throws RemoteException {
        super(); // Tự động export đối tượng ra cổng TCP ẩn
    }

    @Override
    public String sayHello(String name) throws RemoteException {
        // In log thứ tự pipeline Stub -> Skeleton -> Server
        System.out.println("-> [Bước 1: Stub]     Client Marshalling tham số: \"" + name + "\"");
        System.out.println("-> [Bước 2: Skeleton] Server Unmarshalling thành công");
        System.out.println("-> [Bước 3: Server]   HelloImpl.sayHello(\"" + name + "\") đang thực thi");
        return "Hello, " + name + "!";
    }
}
```
- **Kế thừa `UnicastRemoteObject`:** Khi hàm khởi tạo `super()` chạy, JVM sẽ tạo một Socket Server ẩn để lắng nghe các kết nối TCP từ Stub.
- Ghi nhận thông tin máy Client gọi đến bằng hàm: `RemoteServer.getClientHost()`.

### 2.3. Lớp `RMIServer.java`
- Khởi động RMI Registry nội bộ trong tiến trình:
  ```java
  Registry registry = LocateRegistry.createRegistry(1099); // Khởi tạo registry tại port 1099
  HelloImpl helloService = new HelloImpl(); // Tạo đối tượng
  registry.rebind("HelloService", helloService); // Đăng ký service vào Registry
  ```
- Việc dùng `LocateRegistry.createRegistry(1099)` trực tiếp trong code giúp đồ án tự vận hành mà không cần người dùng phải mở cmd ngoài gõ lệnh `rmiregistry` thủ công.

### 2.4. Lớp `RMIClient.java`
- Tra cứu và gọi phương thức:
  ```java
  Registry registry = LocateRegistry.getRegistry("localhost", 1099);
  HelloInterface stub = (HelloInterface) registry.lookup("HelloService"); // Tải Stub
  String result = stub.sayHello("SinhVien"); // Gọi hàm từ xa
  System.out.println("Remote says: " + result);
  ```

---

## 3. KỊCH BẢN DEMO BÁO CÁO TRƯỚC HỘI ĐỒNG

1. **Bước 1 (Khởi động RMI Server):**
   - Chạy `RMIServer` (`java -cp out nhanh6.RMIServer`) hoặc dùng `ServerForm`.
   - Quan sát log:
     - `RMI Registry đã được tạo và khởi động trên cổng TCP: 1099`.
     - `Đã đăng ký thành công service 'HelloService' vào RMI Registry`.
2. **Bước 2 (Chạy RMI Client & Quan sát kết quả):**
   - Chạy `RMIClient` (`java -cp out nhanh6.RMIClient`) hoặc dùng `ClientForm`.
   - Màn hình Client in ra chính xác theo đặc tả đề bài:
     ```
     Remote says: Hello, SinhVien!
     ```
3. **Bước 3 (Chỉ ra đường ống Pipeline trên Console Server):**
   - Chỉ vào màn hình Console Server để ghi điểm với Thầy/Cô:
     ```
     [RMI Server Pipeline] Có lời gọi phương thức từ xa đến sayHello():
     -> [Bước 1: Stub]     Client-side Stub thực hiện Marshalling tham số: "SinhVien"
     -> [Bước 2: Skeleton] Server-side Skeleton nhận dữ liệu từ mạng và Unmarshalling thành công
     -> [Bước 3: Server]   HelloImpl.sayHello("SinhVien") đang thực thi trên Server JVM
     ==> Thứ tự xử lý: Stub -> Skeleton -> HelloImpl.sayHello()
     ```
   - Trình bày: *"Thưa Thầy/Cô, mã nguồn đã mô phỏng và log rõ ràng từng bước trung gian từ khi Client đóng gói tham số (Marshalling) cho đến khi Skeleton phía Server giải mã và thực thi."*

---

## 4. BỘ CÂU HỎI VẤN ĐÁP THƯỜNG GẶP & ĐÁP ÁN MẪU XUẤT SẮC

### Nhóm 1: Bản chất Kiến trúc RMI & Tính toán Phân tán

#### Câu 1: Marshalling và Unmarshalling trong Java RMI là gì? Cơ chế này dựa trên công nghệ nào của Java?
- **Đáp án:**
  - **Marshalling (Đóng gói / Tuần tự hóa):** Là quá trình chuyển đổi đối tượng Java, các tham số của phương thức hoặc giá trị trả về thành một **luồng byte nhị phân** có thể truyền tải qua mạng Socket.
  - **Unmarshalling (Giải mã / Giải tuần tự hóa):** Là quá trình nhận luồng byte từ mạng và tái dựng lại thành một **đối tượng Java hoàn chỉnh trong bộ nhớ** của JVM bên nhận.
  - **Công nghệ nền tảng:** Quá trình này dựa hoàn toàn trên cơ chế **Java Object Serialization** (thông qua hai lớp `ObjectOutputStream` và `ObjectInputStream`). Mọi đối tượng truyền qua RMI (trừ các kiểu nguyên thủy) bắt buộc phải triển khai giao diện `java.io.Serializable`.

#### Câu 2: RMI Registry có vai trò gì? Nếu sau khi Client đã lookup() lấy được Stub thành công mà ta tắt RMI Registry đi thì Client có tiếp tục gọi sayHello() được không? Tại sao?
- **Đáp án:**
  - **Vai trò:** RMI Registry là dịch vụ danh bạ trung gian (Lookup Service), giúp Client ánh xạ từ tên dịch vụ (`"HelloService"`) sang bản sao Stub của đối tượng từ xa.
  - **Nếu tắt RMI Registry sau khi Client đã lấy được Stub:**
    - **Client VẪN GỌI ĐƯỢC hàm `sayHello()` bình thường!**
    - *Giải thích bản chất:* Trong đối tượng Stub mà Client tải về đã chứa sẵn thông tin liên kết từ xa (**RemoteRef**) bao gồm địa chỉ IP và số cổng TCP trực tiếp của đối tượng `HelloImpl` trên Server.
    - Khi Client gọi hàm `sayHello()`, Stub kết nối **trực tiếp** tới Server Object chứ không đi qua RMI Registry nữa. Registry chỉ cần thiết ở giai đoạn tìm kiếm ban đầu.

#### Câu 3: Giao thức JRMP trong Java RMI là gì? Nó hoạt động ở tầng nào của mô hình OSI?
- **Đáp án:**
  - **JRMP (Java Remote Method Protocol):** Là giao thức mạng độc quyền của Java được thiết kế riêng để phục vụ việc triệu gọi phương thức từ xa giữa các máy ảo JVM với nhau.
  - **Vị trí trong OSI:** JRMP hoạt động ở **Tầng Ứng dụng (Application Layer)**, chạy bên trên giao thức truyền dẫn tin cậy **TCP ở Tầng Giao vận (Transport Layer)**.

---

### Nhóm 2: Mổ xẻ Mã nguồn & Thiết kế Lớp

#### Câu 4: Tại sao trong HelloInterface mọi phương thức đều bắt buộc phải khai báo `throws RemoteException`?
- **Đáp án:**
  - Vì trong môi trường phân tán (Distributed System), khác với lời gọi hàm cục bộ trong cùng 1 máy tính, các lời gọi từ xa phải đi qua hạ tầng mạng.
  - Lời gọi từ xa có thể thất bại vì hàng chục lý do ngoài tầm kiểm soát của ứng dụng: Đứt cáp mạng, router bị sập, firewall chặn cổng, Server bị crash, lỗi DNS, lỗi tuần tự hóa đối tượng.
  - `RemoteException` là một **Checked Exception**. Java ép buộc lập trình viên phải khai báo nó trong Interface và viết khối `try-catch` khi gọi hàm để đảm bảo ứng dụng có phương án xử lý sự cố mạng, ngăn ngừa ứng dụng bị sập đột ngột.

#### Câu 5: Từ Java 5 trở đi, công cụ rmic để tạo Stub và Skeleton trước khi biên dịch có còn bắt buộc không? Cơ chế hiện tại hoạt động ra sao?
- **Đáp án:**
  - **Không còn bắt buộc.**
  - Trước Java 5, lập trình viên phải chạy lệnh `rmic nhanh6.HelloImpl` để sinh ra 2 file mã bytecode riêng là `HelloImpl_Stub.class` và `HelloImpl_Skel.class`.
  - Từ Java 5 trở đi, JVM tích hợp cơ chế **Dynamic Proxy (Proxy động)**. Khi Server export đối tượng `UnicastRemoteObject`, JVM sẽ tự động sinh Stub trong bộ nhớ runtime thông qua `java.lang.reflect.Proxy`. Phía Server sử dụng trực tiếp cơ chế phản chiếu (Reflection) và `Dispatcher` để chuyển tiếp lời gọi mà không cần file Skeleton tĩnh.
  - Tuy nhiên, về mặt khái niệm kiến trúc, mô hình vẫn tuân thủ đầy đủ 3 bước: **Stub (Client) $\rightarrow$ Dispatcher/Skeleton (Server) $\rightarrow$ Remote Implementation**.

#### Câu 6: Làm thế nào để truyền một Object tự định nghĩa (ví dụ: `User(name, age)`) qua phương thức của RMI?
- **Đáp án:**
  - Có 2 cách tùy thuộc vào mục đích:
    1. **Truyền theo giá trị (Pass-by-value):** Lớp `User` bắt buộc phải triển khai giao diện `implements java.io.Serializable`. Toàn bộ dữ liệu của đối tượng sẽ được đóng gói (serialize) thành luồng byte, gửi qua mạng và tạo ra một bản sao (Clone) độc lập trên JVM bên kia.
    2. **Truyền theo tham chiếu (Pass-by-reference):** Lớp `User` triển khai một giao diện con kế thừa `Remote` và kế thừa `UnicastRemoteObject`. Khi truyền qua mạng, thứ được gửi đi không phải là dữ liệu của User mà là một bản sao **Stub của User**, cho phép bên nhận có thể gọi ngược lại các phương thức của đối tượng User đó từ xa (Cơ chế Callback).
