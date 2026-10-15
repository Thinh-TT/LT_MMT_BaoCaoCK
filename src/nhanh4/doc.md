# CẨM NANG VẤN ĐÁP CHUYÊN SÂU – NHÁNH 4
## CHỦ ĐỀ: KIẾN TRÚC CLIENT/SERVER VÀ VAI TRÒ CỦA SOCKET TRONG TRUYỀN THÔNG MẠNG (TCP SOCKET)

> **Package:** `nhanh4`  
> **Các lớp chính:** `TCPServer.java`, `TCPClient.java`, `TCPServerListener.java`, `ServerForm.java`, `ClientForm.java`  
> **Cổng dịch vụ:** TCP `9090`  
> **Mục tiêu:** Nắm vững kiến trúc Client/Server, bản chất Socket ở tầng Transport, quá trình bắt tay 3 bước (3-Way Handshake), vòng đời kết nối TCP, chu trình ngắt kết nối và trả lời xuất sắc các câu hỏi vấn đáp.

---

## 1. TỔNG QUAN LÝ THUYẾT CỐT LÕI

### 1.1. Mô hình Client/Server và Khái niệm Socket
- **Mô hình Client/Server (Khách/Chủ):**
  - **Server (Máy chủ):** Là tiến trình thụ động (Passive role). Khởi động trước, mở một cổng dịch vụ cố định (Port `9090`), lắng nghe và chờ đợi các yêu cầu từ Client.
  - **Client (Máy khách):** Là tiến trình chủ động (Active role). Khi cần trao đổi, nó chủ động khởi tạo kết nối đến địa chỉ IP và Port đã biết trước của Server.
- **Bản chất của Socket:**
  - Socket là một giao diện lập trình trừu tượng (API) do Hệ điều hành cung cấp (bắt nguồn từ Berkeley Sockets API trên UNIX).
  - Đối với ứng dụng Java, Socket là chiếc "ống nối" logic giữa 2 tiến trình. Dữ liệu được đẩy vào một đầu bằng `OutputStream` và chảy ra ở đầu bên kia bằng `InputStream`.
  - Một kết nối TCP được định danh duy nhất bởi bộ tứ (4-tuple):
    $$(\text{IP}_{\text{Client}}, \text{Port}_{\text{Client}}, \text{IP}_{\text{Server}}, \text{Port}_{\text{Server}})$$

### 1.2. Quá trình Bắt tay 3 bước (TCP Three-Way Handshake)

Trước khi bất kỳ dữ liệu ứng dụng nào được truyền qua lại, tầng Transport của Hệ điều hành phải hoàn tất thủ tục bắt tay 3 bước để đồng bộ số thứ tự chuỗi (**Sequence Number**) và kiểm tra tính sẵn sàng của cả hai đầu:

```
          TCP CLIENT                                            TCP SERVER
     (Socket("localhost", 9090))                           (ServerSocket(9090))
              |                                                     |
       [Trạng thái: CLOSED]                                  [Trạng thái: LISTEN]
              |                                                     |
              | ---------- Bước 1: Gói SYN (Seq = X) -------------> | (Nhận SYN)
       [Trạng thái: SYN_SENT]                                [Trạng thái: SYN_RCVD]
              |                                                     |
              | <--- Bước 2: Gói SYN-ACK (Seq = Y, Ack = X + 1) --- |
       (Nhận SYN-ACK)                                               |
    [Trạng thái: ESTABLISHED]                                       |
              |                                                     |
              | ---------- Bước 3: Gói ACK (Ack = Y + 1) ---------> | (Nhận ACK)
              |                                              [Trạng thái: ESTABLISHED]
              |                                                     |
     (Sẵn sàng gửi dữ liệu)                              (serverSocket.accept() mở khóa)
```

1. **Bước 1 (SYN):** Client gửi gói tin có cờ `SYN = 1`, chọn một số thứ tự khởi tạo ngẫu nhiên $\text{Seq} = X$ để thông báo: *"Tôi muốn kết nối và bắt đầu đánh số byte từ X"*.
2. **Bước 2 (SYN-ACK):** Server nhận được, gửi lại gói tin có cờ `SYN = 1, ACK = 1`. Xác nhận đã nhận được $X$ bằng $\text{Ack} = X + 1$, đồng thời đưa ra số thứ tự khởi tạo của riêng mình $\text{Seq} = Y$.
3. **Bước 3 (ACK):** Client gửi lại gói tin có cờ `ACK = 1` với $\text{Ack} = Y + 1$ để xác nhận: *"Tôi đã nhận được số thứ tự của bạn. Kết nối chính thức thông suốt!"*

### 1.3. Chu trình Giải phóng Kết nối 4 bước (TCP 4-Way Teardown)

Khi giao tiếp hoàn tất, kết nối không thể đóng đột ngột mà phải đóng theo 4 bước vì TCP là giao thức song công toàn phần (**Full-Duplex** - dữ liệu có thể chảy độc lập theo cả 2 chiều):
1. **Client $\rightarrow$ Server (FIN):** Client gọi `socket.close()`, gửi cờ `FIN`. Thông báo: *"Tôi đã gửi xong toàn bộ dữ liệu, tôi đóng chiều gửi của tôi"*. (Client vào trạng thái `FIN_WAIT_1`).
2. **Server $\rightarrow$ Client (ACK):** Server nhận `FIN`, gửi `ACK` xác nhận. Chiều gửi của Client đã đóng, nhưng chiều gửi của Server vẫn mở nếu Server còn dữ liệu muốn trả về. (Server vào `CLOSE_WAIT`, Client vào `FIN_WAIT_2`).
3. **Server $\rightarrow$ Client (FIN):** Server gửi nốt dữ liệu còn lại, sau đó gọi `socket.close()`, gửi cờ `FIN` sang Client. (Server vào `LAST_ACK`).
4. **Client $\rightarrow$ Server (ACK):** Client gửi `ACK` xác nhận. Client chuyển sang trạng thái `TIME_WAIT` (thường kéo dài $2 \times \text{MSL} \approx 60 - 120$ giây) để đảm bảo gói tin ACK cuối cùng đến đích an toàn, trước khi giải phóng hoàn toàn cổng.

---

## 2. MỔ XẺ MÃ NGUỒN (CODEBASE WALKTHROUGH)

### 2.1. Lớp `TCPServer.java`
- Khởi tạo ServerSocket:
  ```java
  serverSocket = new ServerSocket(port); // Cổng 9090
  ```
- Vòng lặp chờ Client:
  ```java
  while (isRunning) {
      Socket clientSocket = serverSocket.accept(); // Bị chặn (Block) chờ bắt tay 3 bước
      // Lấy thông tin Client
      String clientIp = clientSocket.getInetAddress().getHostAddress();
      int clientPort = clientSocket.getPort(); // Cổng tạm thời (Ephemeral Port) của client
      ...
  }
  ```
- Luồng đọc chuỗi và phản hồi:
  ```java
  BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
  PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);

  String message = reader.readLine();
  System.out.println("Client says: \"" + message + "\"");
  writer.println("Received OK"); // Phản hồi xác nhận theo đúng đặc tả đề bài
  ```

### 2.2. Lớp `TCPClient.java`
- Khởi tạo kết nối chủ động:
  ```java
  Socket socket = new Socket(host, port); // Bắt đầu gửi gói tin SYN
  ```
- Gửi thông điệp và nhận phản hồi:
  ```java
  writer.println(message); // Gửi chuỗi lên Server
  String response = reader.readLine(); // Chờ Server gửi về "Received OK"
  System.out.println("Server: " + response);
  ```

### 2.3. Lớp `ServerForm.java` & `ClientForm.java`
- Giao diện `ServerForm`:
  - Khởi động TCPServer trên nền `SwingWorker`.
  - Cập nhật log kết nối và danh sách chuỗi nhận được từ Client.
  - Nút "Stop Server": Đóng `serverSocket`, giải phóng port 9090 tức thì.
- Giao diện `ClientForm`:
  - Ô nhập Server IP (`localhost`), Port (`9090`), ô nhập nội dung tin nhắn.
  - Nút "Gửi": Gửi chuỗi qua Socket trên `SwingWorker` để không gây đơ giao diện khi chờ mạng.

---

## 3. KỊCH BẢN DEMO BÁO CÁO TRƯỚC HỘI ĐỒNG

1. **Bước 1 (Khởi động Server):**
   - Mở cửa sổ terminal 1 hoặc `ServerForm` chạy `TCPServer`.
   - Quan sát log: `Server đã khởi động và đang lắng nghe tại cổng TCP: 9090`.
   - Giải thích: *"Server đang dừng tại lệnh `serverSocket.accept()` chờ đợi gói tin SYN từ Client."*
2. **Bước 2 (Kết nối Client & Gửi dữ liệu):**
   - Mở cửa sổ terminal 2 hoặc `ClientForm` chạy `TCPClient`.
   - Nhập chuỗi: `"Xin chào Hội đồng chấm thi!"` và nhấn Enter.
   - Quan sát màn hình Server: In ra ngay lập tức: `Client says: "Xin chào Hội đồng chấm thi!"`.
   - Quan sát màn hình Client: Nhận được phản hồi: `Server: Received OK`.
3. **Bước 3 (Thử nghiệm thứ tự khởi động - Câu hỏi tình huống):**
   - Tắt Server đi, thử chạy Client trước.
   - Quan sát lỗi: `java.net.ConnectException: Connection refused`.
   - Giải thích: *"Thưa Thầy/Cô, do TCP là giao thức hướng kết nối (Connection-oriented), nếu cổng đích chưa mở để tiếp nhận gói tin SYN, hệ điều hành lập tức từ chối và văng ConnectException."*

---

## 4. BỘ CÂU HỎI VẤN ĐÁP THƯỜNG GẶP & ĐÁP ÁN MẪU XUẤT SẮC

### Nhóm 1: Cơ chế Giao thức & Hệ điều hành

#### Câu 1: ServerSocket và Socket khác nhau như thế nào? Tại sao Server lại cần cả hai lớp này?
- **Đáp án:**
  - `ServerSocket` là lớp chuyên trách phía **Server**:
    - Nhiệm vụ duy nhất: Đăng ký cổng cố định (`9090`) với HĐH, đặt cổng ở trạng thái `LISTEN` và tiếp nhận các yêu cầu bắt tay kết nối (`accept()`).
    - `ServerSocket` **không trực tiếp tham gia truyền nhận dữ liệu** với Client.
  - `Socket` là lớp đại diện cho **kênh truyền thông dữ liệu hai chiều thực sự**:
    - Phía Client: Khởi tạo `Socket` để chủ động nối tới Server.
    - Phía Server: Khi `serverSocket.accept()` hoàn tất bắt tay 3 bước thành công, nó sinh ra một đối tượng `Socket` mới. `Socket` này được cấp phát tài nguyên riêng biệt để trao đổi luồng dữ liệu (`InputStream`, `OutputStream`) với Client đó.
  - *Lý do cần cả hai:* Để tách biệt rõ ràng trách nhiệm giữa việc "tiếp tân đón khách" (`ServerSocket`) và việc "phục vụ bàn" (`Socket`), tạo tiền đề cho phép Server phục vụ đồng thời hàng nghìn Client.

#### Câu 2: Ephemeral Port (Cổng tạm thời) phía Client là gì? Tại sao ta không cần chỉ định Port cho TCPClient?
- **Đáp án:**
  - Khi khởi tạo `new Socket("localhost", 9090)`, ta chỉ chỉ định **IP đích và Port đích của Server**.
  - Để một liên kết TCP hợp lệ tồn tại, Client cũng bắt buộc phải có một cổng nguồn (Source Port). Cổng này được Hệ điều hành Client **tự động cấp phát ngẫu nhiên** từ dải cổng động/tạm thời (Ephemeral Ports: `49152 - 65535`).
  - Cổng này chỉ tồn tại trong suốt phiên làm việc của kết nối. Khi kết nối đóng lại, HĐH sẽ thu hồi cổng này để tái cấp phát cho tiến trình khác.

#### Câu 3: Trạng thái TIME_WAIT trong TCP là gì? Tại sao khi vừa tắt Server mà bật lại ngay đôi khi bị lỗi "Address already in use"?
- **Đáp án:**
  - Khi một bên chủ động đóng kết nối (gửi FIN đầu tiên), sau khi gửi gói tin ACK cuối cùng, kết nối chưa được giải phóng ngay mà phải bước vào trạng thái **`TIME_WAIT`** (kéo dài khoảng $1 - 2$ phút, tương đương $2 \times \text{MSL}$ - Maximum Segment Lifetime).
  - *Mục đích của TIME_WAIT:*
    1. Đảm bảo gói tin ACK cuối cùng đến được bên nhận. Nếu gói ACK này bị thất lạc trên mạng, bên kia sẽ gửi lại FIN, bên ở trạng thái TIME_WAIT vẫn còn sống để gửi lại ACK.
    2. Ngăn ngừa việc các gói tin cũ còn lang thang trên mạng bị giao nhầm vào một kết nối mới mở trùng IP và Port.
  - *Hậu quả:* Nếu Server là bên chủ động đóng kết nối trước, cổng 9090 sẽ bị kẹt ở trạng thái `TIME_WAIT`. Nếu khởi động lại Server ngay lập tức, HĐH sẽ báo lỗi `BindException: Address already in use`.
  - *Cách khắc phục trong code:* Bật cờ tái sử dụng cổng trước khi bind: `serverSocket.setReuseAddress(true);`.

---

### Nhóm 2: Mổ xẻ Mã nguồn & Xử lý Ngoại lệ

#### Câu 4: Trong TCPServer.java, phương thức accept() hoạt động theo cơ chế Blocking hay Non-blocking? Điều này có nhược điểm gì?
- **Đáp án:**
  - Hoạt động theo cơ chế **Blocking I/O (Chặn đồng bộ)**. Luồng đang chạy phương thức `accept()` sẽ rơi vào trạng thái ngủ (Sleep/Waiting) chờ ngắt từ Hệ điều hành khi có gói tin SYN đến.
  - *Nhược điểm:* Trong kiến trúc đơn luồng (như phiên bản cơ bản của Nhánh 4), khi một Client kết nối và trao đổi dữ liệu, toàn bộ Server bị dừng lại ở luồng đọc ghi của Client đó. Các Client khác đến sau sẽ không được `accept()`. Đây chính là lý do dự án nâng cấp lên **Nhánh 3 (Multithreaded Server với ThreadPool)** để giải quyết triệt để nhược điểm này.

#### Câu 5: Nếu bên gửi gọi `writer.print("Hello")` mà không có ký tự xuống dòng `\n`, bên nhận gọi `reader.readLine()` có đọc được không? Tại sao?
- **Đáp án:**
  - **Không đọc được (Bên nhận sẽ bị treo/block vĩnh viễn).**
  - *Giải thích:* Phương thức `readLine()` của `BufferedReader` được thiết kế để đọc toàn bộ các ký tự cho đến khi gặp ký tự phân tách dòng: `\n` (Line Feed) hoặc `\r\n` (Carriage Return + Line Feed).
  - Nếu bên gửi chỉ dùng `print("Hello")` mà không gửi kèm `\n` hoặc không gọi `println()`, `BufferedReader` ở bên nhận sẽ cho rằng dòng tin nhắn vẫn chưa kết thúc và tiếp tục dừng chờ dữ liệu tiếp theo từ mạng cho đến khi timeout.

#### Câu 6: Sự khác nhau cơ bản giữa việc gửi dữ liệu văn bản qua Socket (như Nhánh 4) và gửi dữ liệu nhị phân qua Socket là gì?
- **Đáp án:**
  - **Dữ liệu văn bản (Text-based):** Sử dụng `BufferedReader` / `PrintWriter` kết hợp bộ chuyển đổi mã `InputStreamReader(..., StandardCharsets.UTF_8)`. Dữ liệu được cấu trúc theo từng dòng kết thúc bằng `\n`. Rất dễ debug bằng mắt người, nhưng tốn băng thông và có chi phí encode/decode.
  - **Dữ liệu nhị phân (Binary-based):** Sử dụng trực tiếp `DataInputStream` / `DataOutputStream` hoặc `BufferedInputStream` / `BufferedOutputStream`. Dữ liệu được gửi dưới dạng mảng byte nguyên thủy hoặc các kiểu dữ liệu cơ sở (`writeInt()`, `writeDouble()`). Băng thông tối ưu, tốc độ xử lý nhanh, phù hợp truyền tải file, ảnh, luồng âm thanh/video.
