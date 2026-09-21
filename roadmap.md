# Roadmap Thực Thi – Báo Cáo Cuối Kỳ LT-MMT

> **Dự án:** `BaoCaoCK`  
> **Thư mục nguồn:** `src/`  
> **Ngôn ngữ:** Java (JDK 8+)  
> **Cập nhật:** 2026-09-21

---

## Cấu Trúc Package Tổng Quan

```
src/
├── nhanh1/          → Luồng byte và luồng ký tự
├── nhanh2/          → Luồng đệm (Buffered Stream)
├── nhanh3/          → Server đa luồng (Multithreaded Server)
├── nhanh4/          → Client/Server & Socket TCP
├── nhanh5/          → UDP – DatagramSocket / DatagramPacket
└── nhanh6/          → RMI – Stub, Skeleton, Registry
```

---

## Nhánh 1 – Luồng Byte và Luồng Ký Tự

**Package:** `nhanh1`  
**Chủ đề:** Nguyên lý hoạt động và ví dụ minh họa

### Mô tả
So sánh hai cách đọc/ghi file trong Java: **luồng byte** (`InputStream`/`OutputStream`) và **luồng ký tự** (`Reader`/`Writer`). Đo và log thời gian thực thi để thấy sự khác biệt khi xử lý file nhị phân vs file văn bản.

### Cấu trúc file

```
src/nhanh1/
├── ByteStreamDemo.java       # Đọc/ghi file bằng FileInputStream & FileOutputStream
├── CharStreamDemo.java       # Đọc/ghi file bằng FileReader & FileWriter
├── PerformanceComparator.java # So sánh + log thời gian thực thi
└── sample.txt                # File mẫu để demo
```

### Các lớp chính

| Lớp | Chức năng |
|-----|-----------|
| `ByteStreamDemo` | Dùng `FileInputStream` / `FileOutputStream` để đọc & ghi file nhị phân |
| `CharStreamDemo` | Dùng `FileReader` / `FileWriter` để đọc & ghi file ký tự (`.txt`) |
| `PerformanceComparator` | Chạy cả hai, đo `System.nanoTime()`, in bảng so sánh tốc độ |

### Kết quả kỳ vọng
- In ra thời gian đọc (ms/ns) của từng phương pháp.
- Bảng log: `[ByteStream] 12ms | [CharStream] 8ms`.
- Giải thích nguyên nhân sự khác biệt.

---

## Nhánh 2 – Luồng Đệm (Buffered Stream)

**Package:** `nhanh2`  
**Chủ đề:** Tối ưu hóa tốc độ I/O bằng luồng đệm

### Mô tả
Thực nghiệm sao chép file lớn (≥ 10 MB) theo **hai cách**: có và không có `BufferedInputStream`/`BufferedOutputStream`. Ghi nhận thời gian và vẽ biểu đồ (text-based).

### Cấu trúc file

```
src/nhanh2/
├── UnbufferedCopy.java       # Sao chép file KHÔNG dùng buffer
├── BufferedCopy.java         # Sao chép file CÓ dùng BufferedInputStream
└── BenchmarkRunner.java      # Chạy cả hai, tổng hợp kết quả
```

### Các lớp chính

| Lớp | Chức năng |
|-----|-----------|
| `UnbufferedCopy` | Copy từng byte một (không buffer), đo thời gian |
| `BufferedCopy` | Copy qua `BufferedInputStream` + `BufferedOutputStream` |
| `BenchmarkRunner` | Gọi cả hai, in bảng so sánh, vẽ biểu đồ ASCII tốc độ I/O |

### Kết quả kỳ vọng
- Thời gian unbuffered >> buffered (có thể gấp 10–100 lần).
- In biểu đồ dạng text: `[Unbuffered] ████████████ 4200ms` vs `[Buffered] ██ 45ms`.

---

## Nhánh 3 – Server Đa Luồng (Multithreaded Server)

**Package:** `nhanh3`  
**Chủ đề:** Lập trình Server đa luồng phục vụ nhiều Client đồng thời

### Mô tả
Xây dựng **TCP Server** dùng `ThreadPoolExecutor`, mỗi kết nối Client được xử lý bởi một thread riêng trong pool. Server nhận chuỗi từ Client và trả về chuỗi **đảo ngược**.

### Cấu trúc file

```
src/nhanh3/
├── MultiThreadedServer.java  # Server TCP với ThreadPool (Executors.newFixedThreadPool)
├── ClientHandler.java        # Runnable xử lý từng kết nối client
└── TestClient.java           # Client test, gửi chuỗi và nhận chuỗi đảo ngược
```

### Các lớp chính

| Lớp | Chức năng |
|-----|-----------|
| `MultiThreadedServer` | Lắng nghe cổng TCP, submit mỗi kết nối vào `ThreadPool` |
| `ClientHandler` | `implements Runnable`, đọc chuỗi → đảo ngược → gửi lại |
| `TestClient` | Kết nối tới Server, gửi chuỗi, in phản hồi |

### Kết quả kỳ vọng
- Mở ≥ 3 cửa sổ `TestClient` đồng thời.
- Server phản hồi đúng chuỗi đảo ngược cho từng client, không bị lẫn lộn.
- Log server: `[Thread-1] Received: "hello" → Sending: "olleh"`.

---

## Nhánh 4 – Kiến Trúc Client/Server & Socket TCP

**Package:** `nhanh4`  
**Chủ đề:** Lập trình Socket mạng theo mô hình Client/Server cơ bản

### Mô tả
Xây dựng cặp **TCP Client/Server** đơn giản nhất: Client gửi một chuỗi, Server in ra console và phản hồi `"Received OK"`.

### Cấu trúc file

```
src/nhanh4/
├── TCPServer.java            # Server lắng nghe, nhận chuỗi, phản hồi "Received OK"
└── TCPClient.java            # Client kết nối, gửi chuỗi, in phản hồi
```

### Các lớp chính

| Lớp | Chức năng |
|-----|-----------|
| `TCPServer` | `ServerSocket` → `accept()` → đọc dữ liệu → in → gửi `"Received OK"` |
| `TCPClient` | `Socket` → gửi chuỗi nhập từ console → nhận & in phản hồi |

### Kết quả kỳ vọng
- Chạy `TCPServer` trước, sau đó chạy `TCPClient`.
- Hai cửa sổ console giao tiếp thành công qua TCP port `9090`.
- Server console: `Client says: "Xin chao"` → Client console: `Server: Received OK`.

---

## Nhánh 5 – UDP: DatagramSocket / DatagramPacket

**Package:** `nhanh5`  
**Chủ đề:** Truyền nhận dữ liệu không kết nối với UDP

### Mô tả
Demo giao tiếp **UDP Ping–Pong**: Client gửi gói tin `"Ping"`, Server nhận và phản hồi `"Pong"`. Kiểm tra port bằng `netstat`.

### Cấu trúc file

```
src/nhanh5/
├── UDPServer.java            # Lắng nghe UDP, nhận "Ping", gửi lại "Pong"
└── UDPClient.java            # Gửi "Ping" qua DatagramSocket, nhận "Pong"
```

### Các lớp chính

| Lớp | Chức năng |
|-----|-----------|
| `UDPServer` | `DatagramSocket(port)` → nhận `DatagramPacket` → gửi phản hồi `"Pong"` |
| `UDPClient` | Tạo `DatagramSocket`, đóng gói `"Ping"` → gửi → nhận & in `"Pong"` |

### Kết quả kỳ vọng
- Client in: `Sent: Ping → Received: Pong`.
- Kiểm tra `netstat -ano` (Windows) thấy port UDP đang mở.
- Ghi chú sự khác biệt UDP vs TCP (không kết nối, có thể mất gói).

---

## Nhánh 6 – RMI: Stub, Skeleton và Registry

**Package:** `nhanh6`  
**Chủ đề:** Gọi phương thức từ xa (Remote Method Invocation)

### Mô tả
Tách mã thành **3 phần riêng biệt**: Interface RMI, Server đăng ký vào `rmiregistry`, và Client gọi phương thức từ xa. Minh họa luồng: Client → Stub → Network → Skeleton → Server Implementation.

### Cấu trúc file

```
src/nhanh6/
├── HelloInterface.java       # Remote Interface (extends Remote)
├── HelloImpl.java            # Server-side: implements HelloInterface + UnicastRemoteObject
├── RMIServer.java            # Đăng ký service vào rmiregistry
└── RMIClient.java            # Lookup registry → gọi phương thức từ xa
```

### Các lớp chính

| Lớp | Chức năng |
|-----|-----------|
| `HelloInterface` | Khai báo phương thức `sayHello(String name)` — `extends Remote` |
| `HelloImpl` | Triển khai `sayHello()`, `extends UnicastRemoteObject` |
| `RMIServer` | `LocateRegistry.createRegistry(1099)` → bind `HelloImpl` |
| `RMIClient` | `LocateRegistry.getRegistry()` → lookup → gọi `sayHello()` → in kết quả |

### Thứ tự chạy
1. Biên dịch toàn bộ package.
2. Chạy `RMIServer` → Registry khởi động trên port `1099`.
3. Chạy `RMIClient` → gọi `sayHello("SinhVien")`.
4. Server log thứ tự: `Stub → Skeleton → HelloImpl.sayHello()`.

### Kết quả kỳ vọng
- Client in: `Remote says: Hello, SinhVien!`
- Server console log rõ thứ tự **Stub – Skeleton – Server**.

---

## Bảng Tổng Hợp Nhanh

| Nhánh | Package | Lớp chính | Port/API |
|-------|---------|------------|----------|
| 1 | `nhanh1` | `ByteStreamDemo`, `CharStreamDemo`, `PerformanceComparator` | File I/O |
| 2 | `nhanh2` | `UnbufferedCopy`, `BufferedCopy`, `BenchmarkRunner` | File I/O |
| 3 | `nhanh3` | `MultiThreadedServer`, `ClientHandler`, `TestClient` | TCP `8080` |
| 4 | `nhanh4` | `TCPServer`, `TCPClient` | TCP `9090` |
| 5 | `nhanh5` | `UDPServer`, `UDPClient` | UDP `9191` |
| 6 | `nhanh6` | `HelloInterface`, `HelloImpl`, `RMIServer`, `RMIClient` | RMI `1099` |

---

## Thứ Tự Triển Khai Đề Xuất

```
Tuần 1  →  Nhánh 1 + Nhánh 2  (File I/O – nền tảng, không cần mạng)
Tuần 2  →  Nhánh 4             (Socket TCP cơ bản)
Tuần 3  →  Nhánh 3             (Multithreaded Server, nâng cao Nhánh 4)
Tuần 4  →  Nhánh 5             (UDP – đối chiếu với TCP)
Tuần 5  →  Nhánh 6             (RMI – cần hiểu Socket trước)
Tuần 6  →  Ôn tập, viết báo cáo, demo cuối kỳ
```

---

> **Lưu ý:** Mỗi nhánh nên có file `README.md` riêng trong package giải thích cách biên dịch và chạy demo.  
> Sử dụng `javac -d out src/nhanhX/*.java` để biên dịch từng nhánh.
