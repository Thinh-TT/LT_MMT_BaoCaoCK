package nhanh2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Lop thuc hien sao chep tep KHONG dung luong dem (Unbuffered Stream).
 *
 * Nguyen ly:
 * - Doc tung byte mot bang FileInputStream.read() truc tiep tu dia.
 * - Ghi tung byte mot bang FileOutputStream.write() truc tiep xuong dia.
 * - Moi thao tac read()/write() deu kich hoat mot System Call xuong he dieu hanh.
 * - Khong co vung nho dem trung gian (RAM buffer), dan toi toc do rat cham khi xu ly file lon.
 */
public class UnbufferedCopy {

    /**
     * Sao chep tep nguon sang tep dich theo phuong phap tung byte khong dung bo dem.
     *
     * @param srcPath  duong dan tep nguon
     * @param destPath duong dan tep dich
     * @return thoi gian thuc thi (nanosecond), hoac -1 neu co loi
     */
    public static long copy(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            System.err.println("Loi: Tep nguon khong ton tai: " + srcPath);
            return -1;
        }

        long bytesCopied = 0;
        long startTime = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(srcFile);
             FileOutputStream fos = new FileOutputStream(destPath)) {

            int b;
            while ((b = fis.read()) != -1) {
                fos.write(b);
                bytesCopied++;
            }
            fos.flush();

        } catch (IOException e) {
            System.err.println("Loi sao chep Unbuffered: " + e.getMessage());
            return -1;
        }

        long durationNs = System.nanoTime() - startTime;
        double durationMs = durationNs / 1_000_000.0;
        double speedMBs = (durationMs > 0) ? ((double) bytesCopied / (1024 * 1024)) / (durationMs / 1000.0) : 0.0;

        System.out.printf("[Unbuffered] Da copy %,d bytes | Thoi gian: %.2f ms | Toc do: %.2f MB/s\n",
                bytesCopied, durationMs, speedMBs);

        return durationNs;
    }

    /**
     * Sao chep tep nguon sang tep dich khong dung bo dem va tra ve thoi gian tinh bang milliseconds.
     * Phuong thuc nay duoc goi tu GUI Form hoac BenchmarkRunner.
     *
     * @param src  tep nguon
     * @param dest tep dich
     * @return thoi gian thuc thi tinh bang milliseconds (ms), hoac -1 neu co loi
     */
    public static long copyAndMeasure(File src, File dest) {
        if (!src.exists()) {
            System.err.println("Loi: Tep nguon khong ton tai: " + src.getAbsolutePath());
            return -1;
        }

        long bytesCopied = 0;
        long startTime = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {

            int b;
            while ((b = fis.read()) != -1) {
                fos.write(b);
                bytesCopied++;
            }
            fos.flush();

        } catch (IOException e) {
            System.err.println("Loi sao chep Unbuffered: " + e.getMessage());
            return -1;
        }

        long durationNs = System.nanoTime() - startTime;
        long durationMs = durationNs / 1_000_000L;
        double durationMsDouble = durationNs / 1_000_000.0;
        double speedMBs = (durationMsDouble > 0) ? ((double) bytesCopied / (1024 * 1024)) / (durationMsDouble / 1000.0) : 0.0;

        System.out.printf("[Unbuffered] Da copy %,d bytes | Thoi gian: %,d ms (%.2f ms) | Toc do: %.2f MB/s\n",
                bytesCopied, durationMs, durationMsDouble, speedMBs);

        return durationMs;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   DEMO SAO CHEP KHONG DUNG BO DEM (UNBUFFERED)   ");
        System.out.println("==================================================");

        String src = "src/nhanh1/sample.txt";
        String dest = "src/nhanh2/unbuffered_test_copy.txt";

        System.out.println("-> Dang thuc hien sao chep file: " + src);
        long timeNs = copy(src, dest);
        if (timeNs > 0) {
            System.out.println("-> Sao chep hoan tat thanh cong!");
            // Don dep file test nho
            new File(dest).deleteOnExit();
        }
    }
}
