# Danh Sách Bài Tập Báo Cáo Cuối Kỳ

---

## Nhánh 1: Luồng byte và luồng ký tự trong Java
* **Chủ đề:** Nguyên lý hoạt động và ví dụ minh họa
* **Demo:** Ứng dụng đọc/ghi nội dung tệp `.txt` bằng `FileInputStream` và `FileReader`.
* **Kết quả:** So sánh tốc độ đọc file nhị phân vs ký tự, log thời gian thực thi.

---

## Nhánh 2: Ứng dụng kỹ thuật luồng đệm (Buffered Stream) để tối ưu tốc độ
* **Chủ đề:** Tối ưu hóa tốc độ I/O bằng luồng đệm
* **Demo:** Viết hai chương trình sao chép tệp lớn có/không dùng `BufferedInputStream`.
* **Kết quả:** So sánh thời gian xử lý, in biểu đồ tốc độ I/O.

---

## Nhánh 3: Thiết kế Server đa luồng phục vụ nhiều Client đồng thời
* **Chủ đề:** Lập trình Server đa luồng (Multithreaded Server)
* **Demo:** Server TCP dùng `ThreadPool`, mỗi Client gửi chuỗi đến Server trả chuỗi đảo ngược.
* **Kết quả:** Chạy ít nhất 3 Client song song, Server vẫn phản hồi đúng từng Client.

---

## Nhánh 4: Kiến trúc Client/Server và vai trò của Socket trong truyền thông mạng
* **Chủ đề:** Lập trình Socket mạng theo mô hình Client/Server
* **Demo:** Mô hình Client gửi chuỗi đến Server in ra console, phản hồi `"Received OK"`.
* **Kết quả:** Hai cửa sổ console giao tiếp thành công qua port TCP.

---

## Nhánh 5: Nguyên lý hoạt động của UDP và các lớp DatagramSocket / DatagramPacket
* **Chủ đề:** Truyền nhận dữ liệu không kết nối với UDP
* **Demo:** Ứng dụng gửi chuỗi `"Ping"` ➔ nhận `"Pong"` qua UDP giữa hai máy.
* **Kết quả:** Hiển thị phản hồi chính xác, kiểm tra port bằng `netstat`.

---

## Nhánh 6: Kiến trúc RMI: Vai trò của Stub, Skeleton và Registry
* **Chủ đề:** Gọi phương thức từ xa (Remote Method Invocation)
* **Demo:** Sinh viên tự tách mã thành 3 phần: `Interface`, `Server`, `Client`, chạy qua `rmiregistry`.
* **Kết quả:** Gọi hàm từ xa thành công, hiển thị thứ tự Stub – Skeleton – Server.
