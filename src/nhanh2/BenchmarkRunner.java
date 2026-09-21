package nhanh2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Chuong trinh Benchmark so sanh toc do sao chep tep giua Unbuffered va Buffered Stream.
 *
 * Chuc nang:
 * 1. Tu dong tao file thu nghiem dung luong lon (mac dinh 5 MB).
 * 2. Do thoi gian sao chep bang UnbufferedCopy (FileInputStream / FileOutputStream).
 * 3. Do thoi gian sao chep bang BufferedCopy (BufferedInputStream / BufferedOutputStream).
 * 4. In bang so sanh chi tiet, tinh he so tang toc (Speedup).
 * 5. Ve bieu do ASCII Bar Chart truc quan toc do I/O va thoi gian thuc thi.
 * 6. Phan tich nguyen ly he dieu hanh va System Call.
 */
public class BenchmarkRunner {

    private static final String TEST_LARGE_FILE = "src/nhanh2/benchmark_source_5mb.dat";
    private static final String UNBUFFERED_DEST = "src/nhanh2/unbuffered_out.dat";
    private static final String BUFFERED_DEST = "src/nhanh2/buffered_out.dat";
    private static final int DEFAULT_FILE_SIZE_MB = 5;

    /**
     * Tu dong tao tep thu nghiem co dung luong lon (tinh bang MB).
     */
    public static void generateLargeFile(String filePath, int sizeInMB) throws IOException {
        File file = new File(filePath);
        long targetBytes = (long) sizeInMB * 1024 * 1024;

        if (file.exists() && file.length() >= targetBytes) {
            return; // Tep da ton tai voi dung luong phu hop
        }

        System.out.printf("-> Dang tao tep thu nghiem dung luong lon (~%d MB)... ", sizeInMB);
        byte[] block = new byte[65536]; // 64 KB block
        new Random(42).nextBytes(block);

        long bytesWritten = 0;
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            while (bytesWritten < targetBytes) {
                int toWrite = (int) Math.min(block.length, targetBytes - bytesWritten);
                bos.write(block, 0, toWrite);
                bytesWritten += toWrite;
            }
            bos.flush();
        }
        System.out.printf("Hoan tat! (Kich thuoc: %,d bytes)\n", file.length());
    }

    /**
     * In bang tong hop so sanh hieu nang.
     */
    public static void printComparisonTable(long fileSizeBytes, long timeUnbufferedNs, long timeBufferedNs) {
        double timeUnbufferedMs = timeUnbufferedNs / 1_000_000.0;
        double timeBufferedMs = timeBufferedNs / 1_000_000.0;
        double sizeMB = (double) fileSizeBytes / (1024 * 1024);

        double speedUnbuffered = (timeUnbufferedMs > 0) ? (sizeMB / (timeUnbufferedMs / 1000.0)) : 0.0;
        double speedBuffered = (timeBufferedMs > 0) ? (sizeMB / (timeBufferedMs / 1000.0)) : 0.0;
        double speedupFactor = (timeBufferedMs > 0) ? (timeUnbufferedMs / timeBufferedMs) : 0.0;

        System.out.println("\n+---------------------------------------------------------------------------------------------------+");
        System.out.printf("| %-38s | %-12s | %-16s | %-13s | %-9s |\n",
                "Phuong Phap Sao Chep", "Dung Luong", "Thoi Gian (ms)", "Toc Do (MB/s)", "Tang Toc");
        System.out.println("+---------------------------------------------------------------------------------------------------+");

        System.out.printf("| %-38s | %10.2f MB | %13.2f ms | %10.2f MB/s | %8s |\n",
                "1. Unbuffered (Tung byte truc tiep)", sizeMB, timeUnbufferedMs, speedUnbuffered, "1.0x");

        System.out.printf("| %-38s | %10.2f MB | %13.2f ms | %10.2f MB/s | %7.1fx |\n",
                "2. Buffered (Luong dem 8KB RAM)", sizeMB, timeBufferedMs, speedBuffered, speedupFactor);

        System.out.println("+---------------------------------------------------------------------------------------------------+");
    }

    /**
     * Ve bieu do ASCII Bar Chart minh hoa thoi gian xu ly va toc do I/O.
     */
    public static void printAsciiCharts(long timeUnbufferedNs, long timeBufferedNs, long fileSizeBytes) {
        double timeUnbufferedMs = timeUnbufferedNs / 1_000_000.0;
        double timeBufferedMs = timeBufferedNs / 1_000_000.0;
        double sizeMB = (double) fileSizeBytes / (1024 * 1024);

        double speedUnbuffered = (timeUnbufferedMs > 0) ? (sizeMB / (timeUnbufferedMs / 1000.0)) : 0.0;
        double speedBuffered = (timeBufferedMs > 0) ? (sizeMB / (timeBufferedMs / 1000.0)) : 0.0;

        int maxBarLength = 40;

        System.out.println("\n====================== BIEU DO ASCII SO SANH HIU NANG ======================");

        // Bieu do 1: Thoi gian thuc thi (cang ngan cang tot)
        System.out.println("\n[A] BIEU DO THOI GIAN XU LY (ms) - (Thap hon la tot hon):");
        double maxTime = Math.max(timeUnbufferedMs, timeBufferedMs);
        int barUnbufferedTime = (int) Math.round((timeUnbufferedMs / maxTime) * maxBarLength);
        int barBufferedTime = Math.max(1, (int) Math.round((timeBufferedMs / maxTime) * maxBarLength));

        System.out.printf("  %-12s [%s] %10.2f ms\n", "Unbuffered", buildBar(barUnbufferedTime, maxBarLength), timeUnbufferedMs);
        System.out.printf("  %-12s [%s] %10.2f ms\n", "Buffered", buildBar(barBufferedTime, maxBarLength), timeBufferedMs);

        // Bieu do 2: Toc do truyen du lieu I/O (cang dai cang tot)
        System.out.println("\n[B] BIEU DO TOC DO I/O (MB/s) - (Cao hon la tot hon):");
        double maxSpeed = Math.max(speedUnbuffered, speedBuffered);
        int barUnbufferedSpeed = Math.max(1, (int) Math.round((speedUnbuffered / maxSpeed) * maxBarLength));
        int barBufferedSpeed = (int) Math.round((speedBuffered / maxSpeed) * maxBarLength);

        System.out.printf("  %-12s [%s] %10.2f MB/s\n", "Unbuffered", buildBar(barUnbufferedSpeed, maxBarLength), speedUnbuffered);
        System.out.printf("  %-12s [%s] %10.2f MB/s\n", "Buffered", buildBar(barBufferedSpeed, maxBarLength), speedBuffered);

        System.out.println("============================================================================");
    }

    private static String buildBar(int filledLength, int totalLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filledLength; i++) {
            sb.append("#");
        }
        for (int i = filledLength; i < totalLength; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * In phan tich ly thuyet va nguyen ly he dieu hanh.
     */
    public static void printTechnicalAnalysis(long timeUnbufferedNs, long timeBufferedNs) {
        double speedup = (timeBufferedNs > 0) ? (double) timeUnbufferedNs / timeBufferedNs : 0;

        System.out.println("\n======================== PHAN TICH NGUYEN LY HE DTH ========================");
        System.out.println("1. Tai sao Unbuffered Stream lai rat cham?");
        System.out.println("   - Trong chuong trinh Unbuffered, moi thao tac read() va write() 1 byte don le");
        System.out.println("     deu phat sinh mot System Call (ngat he thong) xuong nhan he dieu hanh (Kernel).");
        System.out.println("   - Voi file 5 MB, chuong trinh phai thuc hien hon 5,000,000 lan chuyen doi ngu canh");
        System.out.println("     (Context Switch) giua User Mode va Kernel Mode. Chi phi context switch va");
        System.out.println("     truy xuat phan cung dia vat ly lien tuc khien CPU bi nghen nghiem trong.");

        System.out.println("\n2. Tai sao Buffered Stream lai nhanh vuot troi?");
        System.out.printf("   - Luong dem dem lai toc do nhanh gap khoang %.1f lan!\n", speedup);
        System.out.println("   - BufferedInputStream tao mot vung nho dem trong RAM (mac dinh 8192 bytes = 8 KB).");
        System.out.println("   - Khi can doc, no nap truoc (read-ahead) ca mot block 8 KB tu dia vao RAM");
        System.out.println("     chi trong 1 lan System Call duy nhat. 8191 thao tac read() tiep theo duoc");
        System.out.println("     lay truc tiep tu bo nho RAM (toc do RAM nhanh gap hang nghin lan o dia).");
        System.out.println("   - BufferedOutputStream gom du lieu tren RAM va chi ghi xuong dia khi du 8 KB,");
        System.out.println("     giup tan dung toi da kich thuoc sector/block cua he thong tap tin.");

        System.out.println("\n3. Ket luan cho bai bao cao cuoi ky:");
        System.out.println("   - Trong lap trinh mang va I/O file, luon uu tien boc cac luong co ban bang luong dem");
        System.out.println("     (BufferedInputStream / BufferedOutputStream hoac BufferedReader / BufferedWriter).");
        System.out.println("   - Luon chu dong goi flush() truoc khi dong luong hoac khi can gui du lieu tuc thi.");
        System.out.println("============================================================================");
    }

    public static void main(String[] args) {
        System.out.println("============================================================================");
        System.out.println("   CHUONG TRINH BENCHMARK NHANH 2: UNBUFFERED VS BUFFERED STREAM (JAVA)    ");
        System.out.println("============================================================================");

        try {
            // 1. Khoi tao tep thu nghiem lon (5 MB)
            generateLargeFile(TEST_LARGE_FILE, DEFAULT_FILE_SIZE_MB);
            File sourceFile = new File(TEST_LARGE_FILE);
            long fileSizeBytes = sourceFile.length();
            System.out.printf("-> Kich thuoc tep nguon: %,d bytes (~%.2f MB)\n\n",
                    fileSizeBytes, fileSizeBytes / (1024.0 * 1024.0));

            // 2. Chay thu nghiem 1: Unbuffered Copy
            System.out.println("[Buoc 1/2] Dang chay thu nghiem Unbuffered Copy (Sao chep tung byte)...");
            long timeUnbuffered = UnbufferedCopy.copy(TEST_LARGE_FILE, UNBUFFERED_DEST);

            // 3. Chay thu nghiem 2: Buffered Copy
            System.out.println("\n[Buoc 2/2] Dang chay thu nghiem Buffered Copy (Luong dem 8KB)...");
            long timeBuffered = BufferedCopy.copy(TEST_LARGE_FILE, BUFFERED_DEST);

            // 4. In bang so sanh
            printComparisonTable(fileSizeBytes, timeUnbuffered, timeBuffered);

            // 5. In bieu do ASCII Bar Chart
            printAsciiCharts(timeUnbuffered, timeBuffered, fileSizeBytes);

            // 6. In phan tich nguyen ly
            //printTechnicalAnalysis(timeUnbuffered, timeBuffered);

            // 7. Don dep file dich tam sau khi benchmark de tiet kiem bo nho
            new File(UNBUFFERED_DEST).delete();
            new File(BUFFERED_DEST).delete();
            System.out.println("\n-> Da don dep cac file ket qua tam thanh cong.");

        } catch (Exception e) {
            System.err.println("Loi trong qua trinh chay BenchmarkRunner: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
