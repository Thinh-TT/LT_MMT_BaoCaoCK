package nhanh1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Demo minh hoa co che hoat dong cua Luong Byte (Byte Stream) trong Java.
 * Cac lop dai dien tieu bieu: FileInputStream va FileOutputStream.
 *
 * Dac diem:
 * - Xu ly du lieu nhi phan tho o muc byte (8-bit, gia tri tu 0 den 255 hoac -128 den 127).
 * - Khong tu dong chuyen doi bang ma ky tu (Character Encoding).
 * - Thich hop cho tat ca cac loai tep: file nhi phan (anh, video, audio, exe) va van ban tho.
 */
public class ByteStreamDemo {

    /**
     * Doc tep tuan tu tung byte mot bang FileInputStream.read().
     * Minh hoa viec doc byte tho va hien tuong vo ky tu UTF-8 neu ep kieu truc tiep sang (char).
     *
     * @param filePath duong dan tep can doc
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long readByteByByte(String filePath) {
        System.out.println("\n--- [ByteStream] Doc tung byte mot (FileInputStream.read()) ---");
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Tep khong ton tai: " + filePath);
            return -1;
        }

        long byteCount = 0;
        StringBuilder preview = new StringBuilder();
        long startTime = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(file)) {
            int b;
            while ((b = fis.read()) != -1) {
                byteCount++;
                // Luu mot doan van ban ngan de xem truoc viec ep kieu tho (char)b
                if (preview.length() < 350) {
                    preview.append((char) b);
                }
            }
        } catch (IOException e) {
            System.err.println("Loi doc file ByteStream: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Tong so byte doc duoc: %,d bytes\n", byteCount);
        System.out.printf("-> Thoi gian thuc thi: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);
        System.out.println("-> Xem truoc (ep kieu byte sang char - chu y ky tu da byte se bi vo ma):");
        System.out.println("   \"" + preview.toString().replace("\n", "\\n") + "...\"");

        return duration;
    }

    /**
     * Doc tep theo khoi byte bang mang buffer byte[].
     *
     * @param filePath   duong dan tep can doc
     * @param bufferSize kich thuoc mang dem (vi du 1024, 4096 bytes)
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long readWithBuffer(String filePath, int bufferSize) {
        System.out.printf("\n--- [ByteStream] Doc theo khoi byte array (kich thuoc %d bytes) ---\n", bufferSize);
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Tep khong ton tai: " + filePath);
            return -1;
        }

        long byteCount = 0;
        byte[] buffer = new byte[bufferSize];
        long startTime = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byteCount += bytesRead;
            }
        } catch (IOException e) {
            System.err.println("Loi doc file ByteStream co mang dem: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Tong so byte doc duoc: %,d bytes\n", byteCount);
        System.out.printf("-> Thoi gian thuc thi: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);

        return duration;
    }

    /**
     * Ghi mot mang byte ra tep dich bang FileOutputStream.
     *
     * @param targetPath duong dan tep can ghi
     * @param data       mang byte can ghi
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long writeBytes(String targetPath, byte[] data) {
        System.out.println("\n--- [ByteStream] Ghi du lieu bang FileOutputStream ---");
        long startTime = System.nanoTime();

        try (FileOutputStream fos = new FileOutputStream(targetPath)) {
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            System.err.println("Loi ghi file ByteStream: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Ghi thanh cong %,d bytes ra tep: %s\n", data.length, targetPath);
        System.out.printf("-> Thoi gian ghi: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);

        return duration;
    }

    /**
     * Sao chep tep nhi phan tu nguon sang dich bang FileInputStream va FileOutputStream.
     *
     * @param sourcePath duong dan tep nguon
     * @param destPath   duong dan tep dich
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long copyFile(String sourcePath, String destPath) {
        System.out.println("\n--- [ByteStream] Sao chep tep bang luong byte ---");
        File src = new File(sourcePath);
        if (!src.exists()) {
            System.err.println("Tep nguon khong ton tai: " + sourcePath);
            return -1;
        }

        long startTime = System.nanoTime();
        long totalBytesCopied = 0;

        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(destPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesCopied += bytesRead;
            }
            fos.flush();
        } catch (IOException e) {
            System.err.println("Loi sao chep tep: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Da sao chep %,d bytes tu [%s] sang [%s]\n", totalBytesCopied, sourcePath, destPath);
        System.out.printf("-> Thoi gian sao chep: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);

        return duration;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CHUONG TRINH DEMO LUONG BYTE (BYTE STREAM)     ");
        System.out.println("==================================================");

        String samplePath = "src/nhanh1/sample.txt";
        String copyPath = "src/nhanh1/sample_byte_copy.txt";

        // 1. Doc tung byte
        readByteByByte(samplePath);

        // 2. Doc theo mang dem
        readWithBuffer(samplePath, 1024);

        // 3. Sao chep tep bang byte stream
        copyFile(samplePath, copyPath);

        System.out.println("\n Hoan tat demo ByteStreamDemo!");
    }
}
