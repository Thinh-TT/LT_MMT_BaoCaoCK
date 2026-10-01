# Kế Hoạch Thiết Kế Form GUI – BaoCaoCK

> **Dự án:** `BaoCaoCK`
> **Framework:** Java Swing + NetBeans GUI Builder (`.form` + `.java`)
> **Cập nhật:** 2026-10-01

---

## Quyết Định Thiết Kế

| Câu hỏi | Lựa chọn |
|---|---|
| Framework | Java Swing + NetBeans GUI Builder (`.form` + `.java`) |
| Phạm vi | Tất cả 6 nhánh |
| Số form / nhánh 1 & 2 | 1 form duy nhất mỗi nhánh |
| Số form / nhánh 3–6 | 2 form: `ServerForm` + `ClientForm` |
| Tích hợp backend | Gọi thẳng class backend hiện có trong cùng JVM |
| Theme | Light theme chuẩn Swing mặc định |
| Components bắt buộc | JFileChooser, JTextArea (log), JProgressBar, JTextField, JButton Start/Stop, JTable |

---

## Cấu Trúc File Tổng Quan

```
src/
├── nhanh1/
│   ├── MainForm.java          ← Logic + code non-generated
│   └── MainForm.form          ← XML do NetBeans GUI Builder sinh
│
├── nhanh2/
│   ├── BenchmarkForm.java
│   └── BenchmarkForm.form
│
├── nhanh3/
│   ├── ServerForm.java
│   ├── ServerForm.form
│   ├── ClientForm.java
│   └── ClientForm.form
│
├── nhanh4/
│   ├── ServerForm.java
│   ├── ServerForm.form
│   ├── ClientForm.java
│   └── ClientForm.form
│
├── nhanh5/
│   ├── ServerForm.java
│   ├── ServerForm.form
│   ├── ClientForm.java
│   └── ClientForm.form
│
└── nhanh6/
    ├── ServerForm.java
    ├── ServerForm.form
    ├── ClientForm.java
    └── ClientForm.form
```

---

## Nguyên Tắc Kỹ Thuật Chung

> [!IMPORTANT]
> Tất cả tác vụ blocking (network, file I/O) **phải chạy trên `SwingWorker`** để UI không bị đơ (Event Dispatch Thread).

- **SwingWorker:** Mỗi form chạy logic nặng (server loop, file copy, RMI call) trong `SwingWorker<Void, String>`, dùng `publish()` để gửi log string và `process()` để append vào `JTextArea`.
- **Redirect `System.out`:** Dùng `PrintStream` custom pipe vào `JTextArea` để bắt log từ backend hiện có theo real-time.
- **Stop an toàn:** ServerForm lưu reference `SwingWorker` hoặc `Thread`; nút Stop gọi `worker.cancel(true)` và đóng socket/registry.
- **File `.form`:** Là XML do NetBeans sinh — chỉ mở/chỉnh sửa bằng NetBeans GUI Builder. Toàn bộ logic viết trong vùng **non-generated** của file `.java`.

---

## Chi Tiết Từng Nhánh

---

### Nhánh 1 – `MainForm.java / MainForm.form`

**Chủ đề:** So sánh Byte Stream vs Character Stream

#### Layout

```
┌─────────────────────────────────────────────────────────┐
│  [Chọn File]  [______________ đường dẫn ______________] │
│  [Chạy So Sánh]                         [Dừng]          │
│  JProgressBar ████████████░░░░░░░░░░░░                  │
├──────────────────────────────────────────────────────────┤
│  LOG (JTextArea – scroll, read-only)                     │
│  [ByteStream] Đọc 1 MB xong: 12 ms                       │
│  [CharStream] Đọc 1 MB xong: 8 ms                        │
├──────────────────────────────────────────────────────────┤
│  KẾT QUẢ (JTable)                                        │
│  Phương pháp     │ Thời gian (ms) │ Ghi chú              │
│  ByteStream      │ 12             │ Đọc nhị phân         │
│  CharStream      │ 8              │ Đọc ký tự            │
└──────────────────────────────────────────────────────────┘
```

#### Components

| Component | ID | Mô tả |
|---|---|---|
| `JButton` | `btnChooseFile` | Mở `JFileChooser`, chọn file `.txt` đầu vào |
| `JTextField` | `txtFilePath` | Hiển thị đường dẫn file đã chọn (read-only) |
| `JButton` | `btnRun` | Gọi `PerformanceComparator` trên `SwingWorker` |
| `JButton` | `btnStop` | Huỷ `SwingWorker` đang chạy |
| `JProgressBar` | `progressBar` | Indeterminate khi đang chạy |
| `JTextArea` | `txtLog` | Log từng bước real-time |
| `JTable` | `tblResult` | 2 hàng: ByteStream / CharStream; cột: Phương pháp, Thời gian (ms), Ghi chú |

#### Tích hợp backend

```java
// Trong SwingWorker.doInBackground()
PerformanceComparator.generateBenchmarkFile(path, 1024);
long byteTime = ByteStreamDemo.readAndMeasure(path);   // cần refactor trả về long
long charTime = CharStreamDemo.readAndMeasure(path);   // cần refactor trả về long
publish("[ByteStream] " + byteTime + " ms");
publish("[CharStream] " + charTime + " ms");
```

> [!NOTE]
> Cần thêm method `readAndMeasure(String path): long` vào `ByteStreamDemo` và `CharStreamDemo` để form có thể lấy kết quả số (thay vì chỉ in ra console).

---

### Nhánh 2 – `BenchmarkForm.java / BenchmarkForm.form`

**Chủ đề:** Buffered vs Unbuffered Copy

#### Layout

```
┌─────────────────────────────────────────────────────────┐
│  [Chọn File Nguồn]  [__________ đường dẫn ___________] │
│  [Chạy Benchmark]                        [Dừng]         │
│  JProgressBar (indeterminate khi đang copy)              │
├──────────────────────────────────────────────────────────┤
│  LOG (JTextArea)                                         │
│  [Unbuffered] Đang copy... 4200 ms                       │
│  [Buffered]   Đang copy... 45 ms                         │
├──────────────────────────────────────────────────────────┤
│  KẾT QUẢ (JTable)                                        │
│  Phương pháp │ Thời gian (ms) │ Tốc độ (MB/s)           │
│  Unbuffered  │ 4200           │ 1.19                     │
│  Buffered    │ 45             │ 111.1                    │
└──────────────────────────────────────────────────────────┘
```

#### Components

| Component | ID | Mô tả |
|---|---|---|
| `JButton` | `btnChooseFile` | `JFileChooser` chọn file lớn (>= 5 MB) |
| `JTextField` | `txtFilePath` | Đường dẫn file nguồn |
| `JButton` | `btnRun` | Gọi `BenchmarkRunner` trên `SwingWorker` |
| `JButton` | `btnStop` | Huỷ benchmark |
| `JProgressBar` | `progressBar` | Indeterminate khi đang chạy |
| `JTextArea` | `txtLog` | Log tiến trình copy |
| `JTable` | `tblResult` | 2 hàng: Unbuffered / Buffered; cột: Phương pháp, Thời gian (ms), Tốc độ (MB/s) |

#### Tích hợp backend

```java
// Trong SwingWorker.doInBackground()
long unbufferedMs = UnbufferedCopy.copyAndMeasure(src, destUnbuffered);
long bufferedMs   = BufferedCopy.copyAndMeasure(src, destBuffered);
publish("[Unbuffered] " + unbufferedMs + " ms");
publish("[Buffered]   " + bufferedMs + " ms");
```

> [!NOTE]
> Cần thêm method `copyAndMeasure(File src, File dest): long` vào `UnbufferedCopy` và `BufferedCopy`.

---

### Nhánh 3 – `ServerForm` + `ClientForm`

**Chủ đề:** Multithreaded TCP Server (ThreadPool)

---

#### `ServerForm.java / ServerForm.form`

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│  Port: [8080]   Pool size: [5]   [Start Server] [Stop]  │
│  JProgressBar (running / stopped)                        │
├──────────────────────────────────────────────────────────┤
│  LOG (JTextArea)                                         │
│  [Thread-1] Received: "hello" -> Sending: "olleh"        │
│  [Thread-2] Received: "java"  -> Sending: "avaj"         │
├──────────────────────────────────────────────────────────┤
│  DANH SÁCH CLIENT (JTable)                               │
│  Client ID │ IP Address   │ Thời gian    │ Trạng thái    │
│  1         │ 127.0.0.1    │ 14:30:01     │ Done          │
└──────────────────────────────────────────────────────────┘
```

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtPort` | Port, mặc định `8080` |
| `JTextField` | `txtPoolSize` | Số thread pool, mặc định `5` |
| `JButton` | `btnStart` | Gọi `MultiThreadedServer` trên `SwingWorker` |
| `JButton` | `btnStop` | Ngắt server, đóng `ServerSocket` |
| `JProgressBar` | `progressBar` | Running indicator |
| `JTextArea` | `txtLog` | Log từng client xử lý |
| `JTable` | `tblClients` | Cột: Client ID, IP, Thời gian kết nối, Trạng thái |

---

#### `ClientForm.java / ClientForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtHost` | Mặc định `localhost` |
| `JTextField` | `txtPort` | Mặc định `8080` |
| `JTextField` | `txtMessage` | Chuỗi cần gửi |
| `JButton` | `btnSend` | Gọi `TestClient` gửi 1 lần |
| `JTextArea` | `txtLog` | `Sent: "hello" -> Received: "olleh"` |

#### Tích hợp backend

```java
// ServerForm – SwingWorker
MultiThreadedServer server = new MultiThreadedServer(port, poolSize);
server.start(); // blocking loop bên trong SwingWorker

// ClientForm – gọi trực tiếp
TestClient client = new TestClient(host, port);
String response = client.sendAndReceive(message);
```

---

### Nhánh 4 – `ServerForm` + `ClientForm`

**Chủ đề:** TCP Client/Server cơ bản (Socket)

---

#### `ServerForm.java / ServerForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtPort` | Mặc định `9090` |
| `JButton` | `btnStart` | Gọi `TCPServer.start()` trên `SwingWorker` |
| `JButton` | `btnStop` | Gọi `TCPServer.stop()` |
| `JProgressBar` | `progressBar` | Waiting -> Connected |
| `JTextArea` | `txtLog` | `Client says: "Xin chao"` |

---

#### `ClientForm.java / ClientForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtHost` | Mặc định `localhost` |
| `JTextField` | `txtPort` | Mặc định `9090` |
| `JTextField` | `txtMessage` | Chuỗi cần gửi |
| `JButton` | `btnSend` | Kết nối và gửi |
| `JTextArea` | `txtLog` | `Server: Received OK` |

#### Tích hợp backend

```java
// ServerForm
TCPServer server = new TCPServer(port);
// Chạy trong SwingWorker; server.stop() set flag isRunning = false

// ClientForm
TCPClient client = new TCPClient(host, port);
String reply = client.sendMessage(message); // cần refactor trả về String
```

> [!NOTE]
> Cần refactor `TCPClient` để có method `sendMessage(String msg): String` thay vì chỉ in console.

---

### Nhánh 5 – `ServerForm` + `ClientForm`

**Chủ đề:** UDP DatagramSocket / DatagramPacket (Ping–Pong)

---

#### `ServerForm.java / ServerForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtPort` | Mặc định `9191` |
| `JButton` | `btnStart` | Gọi `UDPServer` trên `SwingWorker` |
| `JButton` | `btnStop` | Đóng `DatagramSocket` |
| `JProgressBar` | `progressBar` | Listening... |
| `JTextArea` | `txtLog` | `Received: "Ping" from 127.0.0.1:51234` |
| `JTable` | `tblPackets` | Cột: Thời gian, Source IP:Port, Nội dung nhận, Phản hồi gửi |

---

#### `ClientForm.java / ClientForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtHost` | Mặc định `localhost` |
| `JTextField` | `txtPort` | Mặc định `9191` |
| `JTextField` | `txtMessage` | Mặc định `Ping` |
| `JSpinner` | `spinCount` | Số lần gửi (1–100), mặc định `1` |
| `JButton` | `btnSend` | Gửi N gói UDP |
| `JTextArea` | `txtLog` | `Sent: Ping -> Received: Pong` |

#### Tích hợp backend

```java
// ServerForm
UDPServer server = new UDPServer(port);
// Chạy trong SwingWorker; stop() đóng DatagramSocket

// ClientForm
for (int i = 0; i < count; i++) {
    String pong = UDPClient.sendAndReceive(host, port, message); // cần refactor
    publish("Sent: " + message + " -> Received: " + pong);
}
```

> [!NOTE]
> Cần refactor `UDPClient` để có method `sendAndReceive(String host, int port, String msg): String`.

---

### Nhánh 6 – `ServerForm` + `ClientForm`

**Chủ đề:** RMI – Stub, Skeleton, Registry

---

#### `ServerForm.java / ServerForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtPort` | Mặc định `1099` |
| `JButton` | `btnStart` | Gọi `RMIServer` -> `createRegistry()` + `bind()` |
| `JButton` | `btnStop` | `unbind()` + dọn registry |
| `JProgressBar` | `progressBar` | Waiting for remote calls... |
| `JTextArea` | `txtLog` | Log pipeline RMI: `-> [Stub] -> [Skeleton] -> [HelloImpl.sayHello()]` |

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│  RMI Registry Port: [1099]  [Start Registry] [Stop]     │
│  JProgressBar (waiting for calls...)                     │
├──────────────────────────────────────────────────────────┤
│  LOG – RMI Pipeline (JTextArea)                          │
│  -> [Buoc 1: Stub]     Marshalling tham so: "SinhVien"  │
│  -> [Buoc 2: Skeleton] Unmarshalling thanh cong          │
│  -> [Buoc 3: Server]   HelloImpl.sayHello() thuc thi     │
│  ==> Thu tu: Stub -> Skeleton -> HelloImpl.sayHello()    │
└──────────────────────────────────────────────────────────┘
```

---

#### `ClientForm.java / ClientForm.form`

| Component | ID | Mô tả |
|---|---|---|
| `JTextField` | `txtHost` | Mặc định `localhost` |
| `JTextField` | `txtPort` | Mặc định `1099` |
| `JTextField` | `txtName` | Tham số cho `sayHello(name)` |
| `JButton` | `btnCall` | Lookup registry -> gọi `sayHello()` |
| `JTextArea` | `txtLog` | `Remote says: Hello, SinhVien!` |
| `JTable` | `tblHistory` | Cột: Tên gửi, Kết quả nhận, Thời gian phản hồi (ms) |

**Layout:**

```
┌─────────────────────────────────────────────────────────┐
│  Host: [localhost]   Port: [1099]                        │
│  Tên:  [SinhVien]    [Gọi Remote Method]                 │
├──────────────────────────────────────────────────────────┤
│  KẾT QUẢ (JTextArea)                                     │
│  Remote says: Hello, SinhVien!                           │
├──────────────────────────────────────────────────────────┤
│  LỊCH SỬ GỌI (JTable)                                    │
│  Tên gửi   │ Kết quả nhận          │ Thời gian (ms)      │
│  SinhVien  │ Hello, SinhVien!      │ 23                  │
└──────────────────────────────────────────────────────────┘
```

#### Tích hợp backend

```java
// ServerForm – SwingWorker
Registry registry = LocateRegistry.createRegistry(port);
HelloInterface stub = new HelloImpl();
registry.bind("HelloService", stub);
publish("[RMI Server] Registry khoi dong tren port " + port);

// ClientForm
Registry registry = LocateRegistry.getRegistry(host, port);
HelloInterface remote = (HelloInterface) registry.lookup("HelloService");
long t0 = System.currentTimeMillis();
String result = remote.sayHello(name);
long elapsed = System.currentTimeMillis() - t0;
publish("Remote says: " + result);
// Thêm hàng vào JTable: name, result, elapsed
```

---

## Danh Sách Refactor Backend Cần Làm

| Nhánh | Class | Thay đổi cần thiết |
|---|---|---|
| 1 | `ByteStreamDemo` | Thêm `readAndMeasure(String path): long` |
| 1 | `CharStreamDemo` | Thêm `readAndMeasure(String path): long` |
| 2 | `UnbufferedCopy` | Thêm `copyAndMeasure(File src, File dest): long` |
| 2 | `BufferedCopy` | Thêm `copyAndMeasure(File src, File dest): long` |
| 4 | `TCPClient` | Thêm `sendMessage(String msg): String` (trả về phản hồi) |
| 5 | `UDPClient` | Thêm `sendAndReceive(String host, int port, String msg): String` |

> [!TIP]
> Có thể giữ nguyên `main()` cũ (để chạy console vẫn được), chỉ thêm method mới bên cạnh. Không phá vỡ code hiện có.

---

> **Ghi chú:** File `.form` là XML do NetBeans GUI Builder sinh tự động khi kéo thả component.
> Toàn bộ logic nghiệp vụ và tích hợp backend chỉ được viết trong phần **non-generated**
> (ngoài khối `// <editor-fold>`) của file `.java` tương ứng.
