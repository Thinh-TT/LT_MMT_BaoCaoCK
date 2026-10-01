# Nhanh 6: Kien Truc RMI - Vai Tro Cua Stub, Skeleton va Registry

## 1. Gioi Thieu
Nhanh 6 nghien cuu va thuc nghiem kien truc **Goi phuong thuc tu xa (Remote Method Invocation - RMI)** trong Java.
RMI cho phep mot doi tuong dang chay tren mot may ao Java (JVM nay) co the trieu goi truc tiep phuong thuc cua mot doi tuong tren mot JVM khac (co the nam tren mot may tinh khac qua mang) giong het nhu mot loi goi phuong thuc cuc bo (Local method call).

---

## 2. Cau Truc Ma Nguon

Ma nguon duoc tach thanh 3 phan ro rang theo dung kien truc RMI:

```
src/nhanh6/
├── HelloInterface.java  # Remote Interface (Khai bao phuong thuc sayHello, extends Remote)
├── HelloImpl.java       # Server Implementation (extends UnicastRemoteObject, thuc thi logic)
├── RMIServer.java       # Khoi dong RMI Registry tren cong 1099 va dang ky (bind) service
├── RMIClient.java       # Tra cuu (lookup) Registry lay Stub va goi phuong thuc tu xa
└── README.md            # Tai lieu huong dan chi tiet va ly thuyet kien truc RMI
```

---

## 3. Kien Truc va Thu Tu Xu Ly (Stub – Skeleton – Server)

```
+-------------------+                                  +-------------------+
|    Client JVM     |                                  |    Server JVM     |
|                   |                                  |                   |
|  [ RMIClient ]    |                                  |  [ RMIServer ]    |
|        |          |                                  |        |          |
|        | goi ham  |                                  |        | dang ky  |
|        v          |                                  |        v          |
|  [ HelloStub ]    | <------ Lay Stub tu Lookup ----- | [ RMI Registry ]  |
|   (Client Proxy)  |                                  |    (Port 1099)    |
|        |          |                                  +-------------------+
|   Marshalling     |                                            ^
|   (Dong goi)      |                                            |
|        v          |                                            |
+--------|----------+                                            |
         | (Gui qua mang - Giao thuc JRMP)                        |
         v                                                       |
+--------|----------+                                            |
|  [ Skeleton / ]   | -------------------------------------------+
|  [ Dispatcher ]   |
|   (Unmarshalling) |
|        |          |
|        v          |
|  [ HelloImpl ]    |  ===> Thuc thi: HelloImpl.sayHello("SinhVien")
|  (Server Object)  |  ===> Thu tu: Stub -> Skeleton -> Server
+-------------------+
```

### Vai tro cua 3 thanh phan:
1. **Stub (Client Proxy):** Nam tren Client JVM, dong vai tro la "dai su" cua Server tren may Client. Stub nhan lenh goi tu Client, thuc hien **Marshalling** (tuan tu hoa tham so) va gui goi tin qua mang.
2. **Skeleton / Dispatcher:** Nam tren Server JVM, tiep nhan goi tin tu mang, thuc hien **Unmarshalling** (giai ma tham so) va chuyen giao loi goi den doi tuong thuc thi that (`HelloImpl`). Sau do, Skeleton nhan gia tri tra ve va gui nguoc lai cho Stub.
3. **RMI Registry:** Dich vu danh muc (Naming Directory) chay mac dinh tren cong `1099`. Day la "so danh ba" noi Server dang ky doi tuong tu xa voi ten dinh danh (`HelloService`) va Client tim kiem (lookup) de tai ve ban sao Stub.

---

## 4. Huong Dan Bien Dich Va Thuc Thi

Mo terminal tai thu muc goc cua du an (`e:/LT-MMT/BaoCaoCK` hoac `e:/LT-MMT/LT_MMT_BaoCaoCK`):

### Buoc 1: Bien dich toan bo package `nhanh6`
```powershell
javac -encoding UTF-8 -d out src/nhanh6/*.java
```

### Buoc 2: Khoi dong RMIServer
Mo mot cua so Terminal va chay:
```powershell
java -cp out nhanh6.RMIServer
```
*Console Server se in:*
```text
=============================================================================
         RMI SERVER - DANG KY DICH VU VAO RMI REGISTRY (PORT 1099)           
=============================================================================
-> RMI Registry da duoc tao va khoi dong tren cong TCP: 1099
-> Da dang ky thanh cong service 'HelloService' vao RMI Registry.
-> Server dang hoat dong va cho Client goi phuong thuc tu xa...
```

### Buoc 3: Chay RMIClient
Mo mot cua so Terminal khac va chay:

#### Cach 1: Chay kiem thu mac dinh (Tham so "SinhVien")
```powershell
java -cp out nhanh6.RMIClient
```
*Console Client se in:*
```text
=============================================================================
            RMI CLIENT - GOI PHUONG THUC TU XA (REMOTE METHOD CALL)          
=============================================================================
-> Dang ket noi toi RMI Registry tai localhost:1099...
-> Dang tra cuu (lookup) service 'HelloService'...
-> Da lay duoc Stub proxy tu Registry thanh cong!
-> Dang goi phuong thuc tu xa sayHello("SinhVien")...

-----------------------------------------------------------------------------
Remote says: Hello, SinhVien!
-----------------------------------------------------------------------------
-> Ket qua: [THANH CONG - GOI HAM TU XA RMI CHUAN XAC]
```

*Console Server se in log ro thu tu xu ly:*
```text
-----------------------------------------------------------------------------
[RMI Server Pipeline] Co loi goi phuong thuc tu xa den sayHello():
-> [Buoc 1: Stub]     Client-side Stub thuc hien Marshalling tham so: "SinhVien"
-> [Buoc 2: Skeleton] Server-side Skeleton nhan du lieu tu mang va Unmarshalling thanh cong
-> [Buoc 3: Server]   HelloImpl.sayHello("SinhVien") dang thuc thi tren Server JVM
==> Thu tu xu ly: Stub -> Skeleton -> HelloImpl.sayHello()
-----------------------------------------------------------------------------
```

#### Cach 2: Chay che do go ban phim tuong tac
```powershell
java -cp out nhanh6.RMIClient --interactive
```

---

## 5. Kiem Tra Port RMI Bang Netstat

Trong khi RMIServer dang chay, kiem tra cong 1099 bang PowerShell:
```powershell
netstat -ano | findstr 1099
```
*Ket qua:*
```text
  TCP    0.0.0.0:1099           0.0.0.0:0              LISTENING       <PID>
  TCP    [::]:1099              [::]:0                 LISTENING       <PID>
```

---

## 6. So Sanh Java RMI vs TCP Socket (Phuc Vu Bao Cao Cuoi Ky)

| Tieu chi | TCP Socket (Nhanh 4) | Java RMI (Nhanh 6) |
| :--- | :--- | :--- |
| **Cap do truutuong** | Cap thap: Lam viec voi Byte stream / Text stream thô | Cap cao: Trieu goi phuong thuc huong doi tuong (OOP) |
| **Giao thuc giao tiep** | Nguoi dung tu dinh nghia giao thuc (format chuoi, ket thuc bang `\n`) | Giao thuc chuan Java Remote Method Protocol (JRMP) |
| **Dong goi du lieu** | Tuan tu hoa thu cong (parse chuoi, cat chuoi) | Tu dong qua co che Java Object Serialization (Marshalling) |
| **Do phuc tap lap trinh** | Phai quan ly Socket, InputStream, OutputStream, luong, ngat ket noi | Cu phap tu nhien nhu goi ham cuc bo, che giau chi tiet mang |
| **Tinh da nen tang** | Cao: Client C++/Python co the noi vao Server Java | Chu yeu trong he sinh thai Java (JVM toi JVM) |
