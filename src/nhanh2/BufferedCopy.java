package nhanh2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Lop thuc hien sao chep tep CO dung luong dem (Buffered Stream).
 *
 * Nguyen ly:
 * - BufferedInputStream boc ngoai FileInputStream, tu dong tao mot mang dem RAM (mac dinh 8192 bytes = 8 KB).
 * - Khi doc du lieu, luong dem nap truoc (read-ahead) ca mot khoi 8 KB tu dia vao RAM trong 1 lan goi he thong duy nhat.
 *   Cac thao tac read() tiep theo duoc lay truc tiep tu bo nho RAM, giam hang nghin lan System Call.
 * - BufferedOutputStream tich luy du lieu tren bo dem RAM va chi ghi xuong dia khi bo dem day hoac khi goi flush()/close().
 */
public class BufferedCopy {

    public static final int DEFAULT_BUFFER_SIZE = 8192; // 8 KB mac dinh cua Java

    /**
     * Sao chep tep dung BufferedInputStream va BufferedOutputStream voi khoi dem.
     *
     * @param srcPath  duong dan tep nguon
     * @param destPath duong dan tep dich
     * @return thoi gian thuc thi (nanosecond), hoac -1 neu co loi
     */
    public static long copy(String srcPath, String destPath) {
        return copyCustomBuffer(srcPath, destPath, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Sao chep tep dung luong dem voi kich thuoc bo dem tuy chon.
     *
     * @param srcPath    duong dan tep nguon
     * @param destPath   duong dan tep dich
     * @param bufferSize kich thuoc bo dem tuy bien (bytes)
     * @return thoi gian thuc thi (nanosecond), hoac -1 neu co loi
     */
    public static long copyCustomBuffer(String srcPath, String destPath, int bufferSize) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            System.err.println("Loi: Tep nguon khong ton tai: " + srcPath);
            return -1;
        }

        long bytesCopied = 0;
        long startTime = System.nanoTime();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile), bufferSize);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destPath), bufferSize)) {

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                bytesCopied += bytesRead;
            }
            bos.flush();

        } catch (IOException e) {
            System.err.println("Loi sao chep Buffered: " + e.getMessage());
            return -1;
        }

        long durationNs = System.nanoTime() - startTime;
        double durationMs = durationNs / 1_000_000.0;
        double speedMBs = (durationMs > 0) ? ((double) bytesCopied / (1024 * 1024)) / (durationMs / 1000.0) : 0.0;

        System.out.printf("[Buffered]   Da copy %,d bytes | Thoi gian: %.2f ms | Toc do: %.2f MB/s\n",
                bytesCopied, durationMs, speedMBs);

        return durationNs;
    }

    /**
     * Sao chep tep dung luong dem va tra ve thoi gian tinh bang milliseconds.
     * Phuong thuc nay duoc goi tu GUI Form hoac BenchmarkRunner.
     *
     * @param src  tep nguon
     * @param dest tep dich
     * @return thoi gian thuc thi tinh bang milliseconds (ms), hoac -1 neu co loi
     */
    public static long copyAndMeasure(File src, File dest) {
        return copyAndMeasureCustomBuffer(src, dest, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Sao chep tep dung luong dem voi kich thuoc bo dem tuy chon va tra ve thoi gian (ms).
     *
     * @param src        tep nguon
     * @param dest       tep dich
     * @param bufferSize kich thuoc mang dem (bytes)
     * @return thoi gian thuc thi tinh bang milliseconds (ms), hoac -1 neu co loi
     */
    public static long copyAndMeasureCustomBuffer(File src, File dest, int bufferSize) {
        if (!src.exists()) {
            System.err.println("Loi: Tep nguon khong ton tai: " + src.getAbsolutePath());
            return -1;
        }

        long bytesCopied = 0;
        long startTime = System.nanoTime();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src), bufferSize);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest), bufferSize)) {

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                bytesCopied += bytesRead;
            }
            bos.flush();

        } catch (IOException e) {
            System.err.println("Loi sao chep Buffered: " + e.getMessage());
            return -1;
        }

        long durationNs = System.nanoTime() - startTime;
        long durationMs = durationNs / 1_000_000L;
        double durationMsDouble = durationNs / 1_000_000.0;
        double speedMBs = (durationMsDouble > 0) ? ((double) bytesCopied / (1024 * 1024)) / (durationMsDouble / 1000.0) : 0.0;

        System.out.printf("[Buffered]   Da copy %,d bytes | Thoi gian: %,d ms (%.2f ms) | Toc do: %.2f MB/s\n",
                bytesCopied, durationMs, durationMsDouble, speedMBs);

        return durationMs;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     DEMO SAO CHEP DUNG BO DEM (BUFFERED STREAM)  ");
        System.out.println("==================================================");

        String src = "src/nhanh1/sample.txt";
        String dest = "src/nhanh2/buffered_test_copy.txt";

        System.out.println("-> Dang thuc hien sao chep file: " + src);
        long timeNs = copy(src, dest);
        if (timeNs > 0) {
            System.out.println("-> Sao chep hoan tat thanh cong!");
            new File(dest).deleteOnExit();
        }
    }
}
