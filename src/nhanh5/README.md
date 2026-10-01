# Nhanh 5: Nguyen Ly Hoat Dong Cua UDP va DatagramSocket / DatagramPacket

## 1. Gioi Thieu
Nhanh 5 minh hoa kien truc truyen nhan du lieu **khong ket noi (Connectionless)** su dung giao thuc **UDP (User Datagram Protocol)** thong qua hai lop co ban trong Java: `DatagramSocket` va `DatagramPacket`.

- **UDPServer:** Khoi tao `DatagramSocket` tren cong UDP `9191`. Khong can thiet lap kenh truyen hay bat tay 3 buoc, Server chi can tao mot mang dem `byte[]` de hung cac goi tin `DatagramPacket` tu bat ky dau truyen den, doc chuoi `"Ping"` va phan hoi lai chuoi `"Pong"`.
- **UDPClient:** Khoi tao `DatagramSocket`, dong goi chuoi `"Ping"` vao mot `DatagramPacket` kem dia chi dich (`localhost:9191`), gui di va cho nhan phan hoi `"Pong"`.
- **Dac ta ket qua:** Client hien thi chuoi: `Sent: Ping -> Received: Pong`.

---

## 2. Cau Truc Ma Nguon

```
src/nhanh5/
├── UDPServer.java  # Server UDP lang nghe cong 9191, nhan "Ping" -> gui "Pong"
├── UDPClient.java  # Client UDP gui DatagramPacket, nhan va in "Sent: Ping -> Received: Pong"
└── README.md       # Tai lieu huong dan chi tiet va ly thuyet so sanh TCP vs UDP
```

---

## 3. So Do Kien Truc Datagram Trong Java

```
[ UDPClient ]                                             [ UDPServer ]
     |                                                         |
DatagramSocket()                                      DatagramSocket(9191)
     |                                                         |
Tao DatagramPacket("Ping", IP, 9191)                           |
     |                                                         |
socket.send(sendPacket) -------- [Goi tin UDP] --------> socket.receive(packet)
     |                                                         |
     |                                                Trich xuat "Ping"
     |                                                Tao DatagramPacket("Pong")
     |                                                         |
socket.receive(recvPacket) <----- [Goi tin UDP] ------- socket.send(sendPacket)
     |
In: Sent: Ping -> Received: Pong
```

---

## 4. Huong Dan Bien Dich Va Thuc Thi

Mo terminal tai thu muc goc cua du an (`e:/LT-MMT/BaoCaoCK` hoac `e:/LT-MMT/LT_MMT_BaoCaoCK`):

### Buoc 1: Bien dich package `nhanh5`
```powershell
javac -encoding UTF-8 -d out src/nhanh5/*.java
```

### Buoc 2: Khoi dong UDPServer
Mo mot cua so Terminal rieng biet va chay:
```powershell
java -cp out nhanh5.UDPServer
```
*Console Server se in:*
```text
=============================================================================
            UDP SERVER - TRUYEN NHAN KHONG KET NOI (PORT 9191)               
=============================================================================
-> UDPServer da khoi dong tren cong UDP: 9191
-> San sang nhan DatagramPacket tu Client... (Nhan Ctrl+C de dung)
```

### Buoc 3: Chay UDPClient

#### Cach 1: Chay chu trinh Ping-Pong tu dong (Khuyen khich)
Mo cua so Terminal thu hai va chay:
```powershell
java -cp out nhanh5.UDPClient
```
*Console Client se in:*
```text
Sent: Ping -> Received: Pong
-> Ket qua: [THANH CONG - DUNG CHUAN DAC TA UDP PING-PONG]
```

#### Cach 2: Chay che do go ban phim tuong tac
```powershell
java -cp out nhanh5.UDPClient --interactive
```
- Go: `Ping` -> Nhan: `Pong`
- Go: `hello` -> Nhan: `Echo: hello`
- Go: `exit` -> Dong ket noi.

---

## 5. Kiem Tra Trang Thai Cong UDP Bang Netstat

Trong khi UDPServer dang chay, mo PowerShell va kiem tra trang thai port:
```powershell
netstat -ano | findstr 9191
```
*Ket qua tra ve:*
```text
  UDP    0.0.0.0:9191           *:*                    <PID_Server>
  UDP    [::]:9191              *:*                    <PID_Server>
```
> **Luu y:** Giao thuc UDP la phi ket noi nen trong cot trang thai se khong co `LISTENING` hay `ESTABLISHED` nhu TCP, ma chi ghi nhan socket UDP dang mo tai cong `9191`.

---

## 6. Bang So Sanh Toan Dien: TCP vs UDP (Phuc Vu Bao Cao Cuoi Ky)

| Tieu chi | TCP (Transmission Control Protocol) | UDP (User Datagram Protocol) |
| :--- | :--- | :--- |
| **Co che ket noi** | Co ket noi (Connection-oriented), bat tay 3 buoc | Khong ket noi (Connectionless), gui truc tiep |
| **Tinh tin cay** | Cao: Co ACK, truyen lai neu mat, dam bao thu tu | Thap: Khong ACK, co the mat goi hoac sai thu tu |
| **Kich thuoc Header** | Lon: 20–60 bytes | Nho: Co dinh 8 bytes |
| **Kiem soat luu luong** | Co: Flow Control (Sliding Window), Congestion Control | Khong co kiem soat luu luong |
| **Don vi truyen** | Luong byte lien tuc (Byte stream) | Cac goi tin doc lap (Datagram packet) |
| **Toc do & Do tre** | Cham hon do co overhead kiem soat | Rat nhanh, do tre cuc thap (Near real-time) |
| **Cac lop Java** | `ServerSocket`, `Socket` | `DatagramSocket`, `DatagramPacket` |
| **Ung dung tieu bieu** | HTTP/HTTPS, FTP, SMTP, SSH (can chinh xac) | DNS, VoIP, Video Streaming, Game Online FPS |
