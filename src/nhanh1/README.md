# Nhánh 1: Luồng Byte và Luồng Ký Tự trong Java

## 1. Giới Thiệu
Nhánh 1 tập trung vào việc làm rõ nguyên lý hoạt động, sự khác biệt bản chất và hiệu năng giữa hai họ luồng I/O cơ bản nhất trong nền tảng Java:
- **Luồng byte (Byte Stream):** Làm việc với dữ liệu nhị phân thô cấp độ 8-bit (`InputStream`, `OutputStream`).
- **Luồng ký tự (Character Stream):** Làm việc với ký tự Unicode 16-bit (`Reader`, `Writer`), tích hợp sẵn bộ chuyển đổi bảng mã ký tự (`Charset Decoder/Encoder`).

---

## 2. Cấu Trúc Mã Nguồn

```
src/nhanh1/
├── ByteStreamDemo.java        # Minh họa đọc/ghi bằng FileInputStream và FileOutputStream
├── CharStreamDemo.java        # Minh họa đọc/ghi bằng FileReader và FileWriter (chuẩn UTF-8)
├── PerformanceComparator.java # Bộ đo benchmark so sánh tốc độ, in bảng log và phân tích
├── sample.txt                 # Tệp văn bản mẫu kiểm tra tính toàn vẹn tiếng Việt có dấu
└── README.md                  # Hướng dẫn chi tiết biên dịch, thực thi và báo cáo
```

---

## 3. Bảng So Sánh Lý Thuyết

| Tiêu chí | Luồng Byte (Byte Stream) | Luồng Ký Tự (Character Stream) |
| :--- | :--- | :--- |
| **Lớp cơ sở trừu tượng** | `InputStream`, `OutputStream` | `Reader`, `Writer` |
| **Lớp thao tác tệp** | `FileInputStream`, `FileOutputStream` | `FileReader`, `FileWriter` |
| **Đơn vị dữ liệu xử lý** | Byte (8-bit binary) | Ký tự Unicode (16-bit char) |
| **Cơ chế mã hóa (Charset)** | Không chuyển đổi (raw data) | Tự động Encode/Decode (UTF-8, UTF-16,...) |
| **Đọc văn bản tiếng Việt** | Dễ vỡ font nếu cast `(char)b` | Hiển thị chính xác ký tự đa byte |
| **Loại tệp phù hợp** | Tệp nhị phân: `.png`, `.mp3`, `.mp4`, `.zip`, `.exe` | Tệp văn bản: `.txt`, `.json`, `.csv`, `.html` |

---

## 4. Hướng Dẫn Biên Dịch và Thực Thi

Mở terminal tại thư mục gốc của dự án (`e:/LT-MMT/BaoCaoCK`):

### Bước 1: Biên dịch toàn bộ package `nhanh1`
```powershell
javac -encoding UTF-8 -d out src/nhanh1/*.java
```

### Bước 2: Chạy thử nghiệm từng lớp

1. **Chạy Demo Luồng Byte:**
   ```powershell
   java "-Dstdout.encoding=UTF-8" -cp out nhanh1.ByteStreamDemo
   ```
   *Quan sát:* Xem trước dữ liệu khi ép kiểu trực tiếp `(char)byte` trên luồng byte sẽ thấy ký tự tiếng Việt bị lỗi font (do mỗi ký tự UTF-8 tiếng Việt gồm 2 đến 3 byte ghép lại).

2. **Chạy Demo Luồng Ký Tự:**
   ```powershell
   java "-Dstdout.encoding=UTF-8" -cp out nhanh1.CharStreamDemo
   ```
   *Quan sát:* FileReader giải mã các byte thành ký tự Unicode hoàn chỉnh, đoạn văn bản tiếng Việt hiển thị rõ ràng, chính xác.

3. **Chạy Chương Trình So Sánh Hiệu Năng (Benchmark):**
   ```powershell
   java "-Dstdout.encoding=UTF-8" -cp out nhanh1.PerformanceComparator
   ```
   *Quan sát:* Chương trình sẽ tự sinh tệp thử nghiệm 1MB, xuất bảng so sánh thời gian thực thi (ms/ns), thông lượng (MB/s) và minh họa trực quan sự vỡ font giữa ByteStream vs CharStream.

---

## 5. Mẫu Kết Quả Log Đo Đạc Thực Tế

```
+-----------------------------------------------------------------------------------------------+
| Phương Pháp Đọc Dữ Liệu                    | Thời Gian (ms)   | Thời Gian (ns) | Tốc Độ (MB/s) |
+-----------------------------------------------------------------------------------------------+
| 1. ByteStream (Đọc từng byte 8-bit)        |      1238.782 ms |  1,238,782,400 |       0.81 MB/s |
| 2. CharStream (Đọc từng ký tự UTF-8)       |        50.314 ms |     50,314,100 |      19.88 MB/s |
| 3. ByteStream (Mảng đệm 1024 bytes)        |         1.653 ms |      1,653,300 |     604.85 MB/s |
| 4. CharStream (Mảng đệm 1024 chars)        |         1.982 ms |      1,981,500 |     504.67 MB/s |
+-----------------------------------------------------------------------------------------------+
```

### Minh họa tính toàn vẹn ký tự:
- **Byte Stream (ép kiểu thô `(char)b`):**
  `Cá»™ng hÃ²a XÃ£ há»™i Chá»§ nghÄ©a Viá»‡t Nam` (Lỗi Mojibake)
- **Character Stream (`FileReader` với UTF-8):**
  `Cộng hòa Xã hội Chủ nghĩa Việt Nam` (Hiển thị 100% chuẩn xác)

---

## 6. Kết Luận Báo Cáo
1. **Tại sao đọc từng byte/ký tự lại chậm?**
   Mỗi lệnh gọi `read()` đơn lẻ phải kích hoạt một lệnh gọi hệ thống (System Call) xuống nhân hệ điều hành. Việc chuyển đổi liên tục giữa User Mode và Kernel Mode gây lãng phí CPU.
2. **Tại sao ByteStream đọc nhanh hơn CharStream trên raw data?**
   ByteStream chuyển byte thô trực tiếp từ tệp vào bộ nhớ mà không cần qua bước xử lý bảng mã. CharStream phải thực hiện giải mã (Decode) dòng byte thành ký tự Unicode 16-bit.
3. **Bài học rút ra:**
   - Luôn sử dụng mảng đệm (buffer) hoặc luồng đệm (`BufferedInputStream` / `BufferedReader` ở Nhánh 2) để tăng tốc độ I/O.
   - Chọn đúng công cụ: File nhị phân dùng ByteStream, file văn bản dùng Character Stream.
