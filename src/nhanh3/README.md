# Nhanh 3: Thiet Ke Server Da Luong Phuc Vu Nhieu Client Dong Thoi

## 1. Gioi Thieu
Nhanh 3 xay dung mot **Server TCP da luong (Multithreaded Server)** trong Java su dung mo hinh **ThreadPool** (`ExecutorService` / `Executors.newFixedThreadPool`). 
Server co kha nang phuc vu nhieu Client dong thoi ma khong bi tac nghen hay tron lan du lieu giua cac phien lam viec.

- **Chuc nang Server:** Nhan chuoi ky tu tu bat ky Client nao, thuc hien dao nguoc chuoi (`StringBuilder.reverse()`) va phan hoi lai chuoi da dao nguoc.
- **Quan ly luong:** Su dung ThreadPool co dinh (10 worker threads) de tai su dung luong, tranh tao luong vo han gay can kiet bo nho (Out Of Memory).

---

## 2. Cau Truc Ma Nguon

```
src/nhanh3/
├── MultiThreadedServer.java  # Server TCP lang nghe cong 8080, quan ly ThreadPool
├── ClientHandler.java        # Lop Runnable xu ly rieng cho tung Client tren mot Thread
├── TestClient.java           # Client TCP (ho tro ca che do go ban phim va gia lap song song)
└── README.md                 # Tai lieu huong dan chi tiet
```

---

## 3. So Do Hoat Dong (Architecture Workflow)

```
[ Client 1 ] ---> Ket noi TCP port 8080 ---> [ ServerSocket.accept() ]
[ Client 2 ] ---> Ket noi TCP port 8080 --->         |
[ Client 3 ] ---> Ket noi TCP port 8080 --->         v
                                          [ ThreadPool Executor ]
                                           /         |         \
                                          v          v          v
                                     [Worker-1]  [Worker-2]  [Worker-3]
                                         |           |           |
                                    ClientHandler ClientHandler ClientHandler
                                    (Dao nguoc)   (Dao nguoc)   (Dao nguoc)
```

---

## 4. Huong Dan Bien Dich Va Thuc Thi

Mo terminal tai thu muc goc cua du an (`e:/LT-MMT/BaoCaoCK`):

### Buoc 1: Bien dich toan bo package `nhanh3`
```powershell
javac -encoding UTF-8 -d out src/nhanh3/*.java
```

### Buoc 2: Khoi dong Server TCP
Mo mot cua so terminal va chay lenh sau:
```powershell
java -cp out nhanh3.MultiThreadedServer
```
*Console Server se bao:*
```
-> Server da khoi dong thanh cong tren cong TCP: 8080
-> ThreadPool khoi tao: 10 luong worker san sang xu ly dong thoi
-> Dang cho Client ket noi den...
```

### Buoc 3: Chay Client thu nghiem

#### Cach 1: Chay gia lap tu dong 3-5 Client song song (Khuyen khich)
Mo them mot cua so terminal moi va chay:
```powershell
java -cp out nhanh3.TestClient --simulate 3
```
*Chuc nang nay se tu dong khoi tao 3 Client cung mot luc gui cac chuoi khac nhau den Server va kiem tra tinh dung dan cua chuoi dao nguoc.*

#### Cach 2: Chay tuong tac thu cong qua nhieu cua so Terminal
Mo 3 cua so terminal khac nhau, tai moi cua so chay:
```powershell
java -cp out nhanh3.TestClient
```
- Tai Terminal Client 1: Go `hello` -> Nhan: `olleh`
- Tai Terminal Client 2: Go `network` -> Nhan: `krowten`
- Tai Terminal Client 3: Go `java socket` -> Nhan: `tekcos avaj`
- Go `exit` tren bat ky client nao de ngat ket noi client do.

---

## 5. Mau Ket Qua Log Tren Server

Khi co 3 Client ket noi va gui du lieu song song, Server se in log ro ten tung Worker Thread:

```text
[Main-Server] Chap nhan ket noi moi #1 tu: /127.0.0.1:54321. Dang chuyen vao ThreadPool...
[Main-Server] Chap nhan ket noi moi #2 tu: /127.0.0.1:54322. Dang chuyen vao ThreadPool...
[Main-Server] Chap nhan ket noi moi #3 tu: /127.0.0.1:54323. Dang chuyen vao ThreadPool...
[pool-1-thread-1] [Client-1] Nhan: "hello world" -> Gui lai: "dlrow olleh"
[pool-1-thread-2] [Client-2] Nhan: "lap trinh mang can ban" -> Gui lai: "nab nac gnam hnirt pal"
[pool-1-thread-3] [Client-3] Nhan: "multithreaded server" -> Gui lai: "revres dedaerhtitlum"
```

---

## 6. Phan Tich Kien Truc Phuc Vu Bao Cao Cuoi Ky

1. **Uu diem cua ThreadPool so voi Thread-per-Client:**
   - **Tiet kiem tai nguyen:** Tao mot Thread trong he dieu hanh ton chi phi cap phat bo nho Stack (thuong 1MB/thread). ThreadPool tai su dung cac luong da tao, tranh viec lien tuc tao moi va huy luong (Garbage Collection).
   - **Chong tan cong DoS (Resource Exhaustion):** Gioi han so luong luong chay dong thoi (vi du: 10 threads). Neu co 100 client den cung luc, 90 client con lai se cho trong hang doi (BlockingQueue) thay vi lam sup do toan bo he thong.
2. **Tinh doc lap va an toan du lieu (Thread Isolation):**
   - Moi doi tuong `ClientHandler` giu Socket rieng biet va bien cuc bo trong ham `run()`, dam bao du lieu chuoi dao nguoc cua Client nay khong the bi ghi de hay anh huong sang Client khac.
