# Nhanh 4: Kien Truc Client/Server va Vai Tro Cua Socket Trong Truyen Thong Mang

## 1. Gioi Thieu
Nhanh 4 minh hoa kien truc co ban nhat cua lap trinh mang: **Mo hinh Client/Server qua giao thuc TCP (Transmission Control Protocol)** su dung cac lop `ServerSocket` va `Socket` trong Java.

- **TCPServer:** Dong vai tro la ung dung lang nghe tren mot cong TCP xac dinh (`9090`), tiep nhan yeu cau ket noi tu Client, nhan chuoi ky tu, in ra man hinh console `Client says: "<chuoi>"` va phan hoi lai chuoi xac nhan `"Received OK"`.
- **TCPClient:** Dong vai tro la ung dung khoi tao ket noi toi Server, cho phep nguoi dung nhap chuoi tu ban phim, gui qua Socket va nhan phan hoi `Server: Received OK`.

---

## 2. Cau Truc Ma Nguon

```
src/nhanh4/
├── ServerForm.java        # Giao dien GUI Server (SwingWorker + NetBeans Builder)
├── ServerForm.form        # XML thiet ke giao dien Server tren NetBeans
├── ClientForm.java        # Giao dien GUI Client (SwingWorker + NetBeans Builder)
├── ClientForm.form        # XML thiet ke giao dien Client tren NetBeans
├── TCPServer.java         # Server TCP lang nghe cong 9090, nhan chuoi va phan hoi "Received OK"
├── TCPServerListener.java # Interface callback su kien cap nhat ServerForm realtime
├── TCPClient.java         # Client TCP ket noi toi Server, gui chuoi va in ket qua
└── README.md              # Tai lieu huong dan chi tiet va ly thuyet kien truc Socket
```

---

## 3. So Do Nguyen Ly Hoat Dong (TCP Socket Lifecycle)

```
        TCPServer (Port 9090)                           TCPClient
                  |                                         |
         ServerSocket(9090)                                 |
                  |                                         |
               accept()  <--- [1. SYN] ----------------- Socket(host, 9090)
                  |      ---> [2. SYN-ACK] ------------>    |
                  |      <--- [3. ACK] -----------------    |
                  |                                         |
     (Ket noi ESTABLISHED)                      (Ket noi ESTABLISHED)
                  |                                         |
          reader.readLine() <--- "Xin chao" ------------ writer.println(...)
                  |                                         |
         In: Client says: "Xin chao"                        |
                  |                                         |
     writer.println("Received OK") ---> "Received OK" -> reader.readLine()
                  |                                         |
                  |                                 In: Server: Received OK
                  |                                         |
          reader.readLine() <--- "exit" ---------------- writer.println("exit")
                  |                                         |
             socket.close() <--- [FIN/ACK] ------------ socket.close()
```

---

## 4. Huong Dan Bien Dich Va Thuc Thi

Mo terminal tai thu muc goc cua du an (`e:/LT-MMT/BaoCaoCK` hoac `e:/LT-MMT/LT_MMT_BaoCaoCK`):

### Buoc 1: Bien dich package `nhanh4`
```powershell
javac -encoding UTF-8 -d out src/nhanh4/*.java
```

### Buoc 2: Chay Server
Mo mot cua so Terminal rieng biet va khoi dong TCPServer:
```powershell
java -cp out nhanh4.TCPServer
```
*Console Server se in:*
```text
=============================================================================
           TCP SERVER - KIEN TRUC CLIENT / SERVER CO BAN (PORT 9090)         
=============================================================================
-> Server da khoi dong va dang lang nghe tai cong TCP: 9090
-> San sang nhan ket noi tu Client... (Nhan Ctrl+C de dung Server)
```

### Buoc 3: Chay Client

#### Cach 1: Kiem thu tu dong (Automated Test)
Mo mot cua so Terminal khac va chay:
```powershell
java -cp out nhanh4.TCPClient --auto "Xin chao"
```

#### Cach 2: Chay tuong tac truc tiep qua 2 cua so Console (Manual Demo)
Mo cua so Terminal thu hai va chay:
```powershell
java -cp out nhanh4.TCPClient
```
- Nhap: `Xin chao`
- Quan sat Console Client hien thi: `Server: Received OK`
- Quan sat Console Server hien thi: `Client says: "Xin chao"`
- Nhap them cac chuoi bat ky nhu: `Lap trinh mang`, `Socket TCP`
- Go: `exit` de dong ket noi an toan.

---

### Buoc 4: Khoi chay giao dien do hoa Swing (GUI Forms)

#### Khoi dong ServerForm (May chu TCP):
Mo terminal va chay:
```powershell
java -cp out nhanh4.ServerForm
```
- Form hien thi cong TCP mac dinh `9090`.
- Bam **Start Server** de lang nghe ket noi tren background thread qua `SwingWorker`.
- Thanh tien trinh chuyen sang trang thai *RUNNING / Listening*.
- Nhat ky hien thi bat tay 3 buoc TCP, cac chuoi nhan duoc tu Client va phan hoi `Received OK`.
- Bang danh sach luu tru toan bo cac ban tin trao doi thoi gian thuc.

#### Khoi dong ClientForm (Client GUI):
Mo them mot terminal va chay:
```powershell
java -cp out nhanh4.ClientForm
```
- Nhap dia chi `localhost` va cong `9090`.
- Nhap chuoi tin nhan (vi du `Xin chao`) roi bam **Gửi (Send)** hoac an phim Enter.
- Bam **Kiểm Thử Tự Động** de chay test case chuan voi chuoi `"Xin chao"`.
- Quan sat phan hoi: `Server: Received OK`, do tre (latency) va lich su luu vao `JTable`.

---

## 5. Kiem Tra Trang Thai Cong Mang Bang Netstat

Trong khi Server dang chay hoac khi Client dang ket noi, mo mot terminal PowerShell va kiem tra trang thai port:
```powershell
netstat -ano | findstr 9090
```
- Khi Server dang cho ket noi:
  ```text
  TCP    0.0.0.0:9090           0.0.0.0:0              LISTENING       <PID>
  ```
- Khi Client dang ket noi va truyen du lieu:
  ```text
  TCP    127.0.0.1:9090         127.0.0.1:54321        ESTABLISHED     <PID_Server>
  TCP    127.0.0.1:54321        127.0.0.1:9090         ESTABLISHED     <PID_Client>
  ```

---

## 6. Phan Tich Kien Truc Phuc Vu Bao Cao Cuoi Ky

1. **Vai tro cua Socket trong mo hinh OSI/TCP-IP:**
   - Socket la diem cuoi (Endpoint) trong kenh truyen thong hai chieu giua hai tien trinh qua mang.
   - Socket la giao dien lap trinh ung dung (API) cau noi giua tang Ung dung (Application Layer) va tang Giao van (Transport Layer).
2. **Kha nang tin cay cua TCP:**
   - Khac voi UDP (khong ket noi, co the mat goi tin), TCP thiet lap ket noi co dinh truoc khi truyen du lieu (Three-way Handshake).
   - Co che ACK va truyen lai dam bao dong du lieu (Byte stream) duoc chuyen giao nguyen ven, dung thu tu va khong bi mat mat.
3. **So sanh Nhanh 4 vs Nhanh 3:**
   - Nhanh 4 la mo hinh tuan tu co ban (Single-threaded Server): xu ly mot client tai mot thoi diem.
   - Nhanh 3 nang cap mo hinh nay len kien truc Da luong voi ThreadPool de phuc vu hang chuc client dong thoi.
