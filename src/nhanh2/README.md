# Nhanh 2: Ung Dung Ky Thuat Luong Dem (Buffered Stream) De Toi Uu Toc Do

## 1. Gioi Thieu
Nhanh 2 thuc nghiem va chung minh hieu qua toi uu hoa toc do I/O cua **Luong Dem (Buffered Stream)** so voi luong I/O thong thuong (Unbuffered Stream) khi thao tac tren cac tep tin dung luong lon (>= 5MB - 10MB).

---

## 2. Cau Truc Ma Nguon

```
src/nhanh2/
├── UnbufferedCopy.java     # Sao chep file KHONG dung buffer (tung byte truc tiep)
├── BufferedCopy.java       # Sao chep file CO dung BufferedInputStream & BufferedOutputStream (8KB)
├── BenchmarkRunner.java    # Chuong trinh dieu phoi do dac, in bang so sanh va bieu do ASCII
└── README.md               # Tai lieu huong dan chi tiet
```

---

## 3. Bang So Sanh Ly Thuyet

| Tieu chi | Khong Dung Bo Dem (Unbuffered) | Co Dung Luong Dem (Buffered Stream) |
| :--- | :--- | :--- |
| **Lop dai dien** | `FileInputStream`, `FileOutputStream` | `BufferedInputStream`, `BufferedOutputStream` |
| **Co che hoat dong** | Truy xuat dia truc tiep moi thao tac `read()` / `write()` | Nap truoc ca block 8 KB vao RAM bo dem (`read-ahead`) |
| **So lan System Call** | N lan (bang dung luong byte cua file) | N / 8192 lan (giam hon 8,000 lan) |
| **Chuyen doi ngu canh** | Lien tuc chuyen giua User Mode va Kernel Mode | Thuc hien chu yeu tren khong gian User Space (RAM) |
| **Hieu qua thuc te** | Rat cham doi voi file lon | Nhanh gap hang tram lan |

---

## 4. Huong Dan Bien Dich Va Thuc Thi

Mo terminal tai thu muc goc cua du an (`e:/LT-MMT/BaoCaoCK`):

### Buoc 1: Bien dich package `nhanh2`
```powershell
javac -encoding UTF-8 -d out src/nhanh2/*.java
```

### Buoc 2: Chay thu nghiem

1. **Chay thu sao chep Unbuffered don le:**
   ```powershell
   java -cp out nhanh2.UnbufferedCopy
   ```

2. **Chay thu sao chep Buffered don le:**
   ```powershell
   java -cp out nhanh2.BufferedCopy
   ```

3. **Chay bo Benchmark tong hop (file 5 MB) va ve bieu do:**
   ```powershell
   java -cp out nhanh2.BenchmarkRunner
   ```

---

## 5. Mau Ket Qua Benchmark Thuc Te

```
+---------------------------------------------------------------------------------------------------+
| Phuong Phap Sao Chep                   | Dung Luong   | Thoi Gian (ms)   | Toc Do (MB/s) | Tang Toc  |
+---------------------------------------------------------------------------------------------------+
| 1. Unbuffered (Tung byte truc tiep)    |      5.00 MB |      25359.66 ms |       0.20 MB/s |     1.0x |
| 2. Buffered (Luong dem 8KB RAM)        |      5.00 MB |         15.38 ms |     325.03 MB/s |  1648.5x |
+---------------------------------------------------------------------------------------------------+
```

### Bieu do ASCII Bar Chart:
```text
[A] BIEU DO THOI GIAN XU LY (ms) - (Thap hon la tot hon):
  Unbuffered   [########################################]   25359.66 ms
  Buffered     [#                                       ]      15.38 ms

[B] BIEU DO TOC DO I/O (MB/s) - (Cao hon la tot hon):
  Unbuffered   [#                                       ]       0.20 MB/s
  Buffered     [########################################]     325.03 MB/s
```

---

## 6. Phan Tich Nguyen Ly He Dieu Hanh
1. **System Call & Context Switching:**
   - Moi lan ung dung Java doc/ghi byte truc tiep tu dia, CPU phai chuyen doi tu User Mode sang Kernel Mode thong qua System Call.
   - Voi file 5 MB, Unbuffered gay ra 5 trieu System Calls, tao overhead cuc lon cho he dieu hanh.
2. **Co che Read-Ahead & Flushing cua Bo Dem:**
   - `BufferedInputStream` khoi tao mot mang dem 8 KB trong RAM. Khi doc byte dau tien, no yeu cau he dieu hanh nap luon 8,192 bytes vao RAM.
   - 8,191 byte tiep theo duoc CPU truy xuat truc tiep tren RAM ma khong can bat ky System Call nao.
   - Nho do, toc do sao chep tang tu 100x den 300x.
