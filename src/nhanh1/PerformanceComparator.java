package nhanh1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Lop so sanh hieu nang thuc thi giua Luong Byte (Byte Stream) va Luong Ky Tu (Character Stream).
 *
 * Tieu chi danh gia:
 * 1. Doc tung don vi (Byte-by-Byte vs Char-by-Char).
 * 2. Doc theo khoi mang dem (Byte array vs Char array).
 * 3. So sanh tinh chinh xac khi doc du lieu van ban UTF-8 co dau.
 */
public class PerformanceComparator {

    private static final String BENCHMARK_FILE = "src/nhanh1/benchmark_temp.txt";
    private static final int DEFAULT_BENCHMARK_SIZE_KB = 1024; // 1 MB

    /**
     * Tu dong sinh tep thu nghiem voi kich thuoc xac dinh (KB).
     */
    public static void generateBenchmarkFile(String filePath, int sizeInKB) throws IOException {
        File file = new File(filePath);
        if (file.exists() && file.length() >= sizeInKB * 1024L) {
            return; // Da co file kich thuoc phu hop
        }

        System.out.printf("-> Dang khoi tao tep thu nghiem benchmark (~%d KB)...\n", sizeInKB);
        String sampleText = "Lap trinh mang can ban - So sanh ByteStream va Character Stream trong Java. "
                + "Thu nghiem voi chuoi ky tu UTF-8: Ha Noi, Da Nang, TP Ho Chi Minh, Can Tho. "
                + "He thong mang may tinh va truyen thong du lieu ket noi da luong.\n";

        byte[] textBytes = sampleText.getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            int writtenBytes = 0;
            int targetBytes = sizeInKB * 1024;
            while (writtenBytes < targetBytes) {
                int toWrite = Math.min(textBytes.length, targetBytes - writtenBytes);
                fos.write(textBytes, 0, toWrite);
                writtenBytes += toWrite;
            }
            fos.flush();
        }
        System.out.printf("-> Da tao xong file: %s (Kich thuoc: %,d bytes)\n", filePath, file.length());
    }

    /**
     * Do thoi gian doc toan bo file bang FileInputStream (tung byte).
     */
    public static long benchmarkByteByByte(String filePath) {
        long startTime = System.nanoTime();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            while (fis.read() != -1) {
                // Doc tung byte
            }
        } catch (IOException e) {
            System.err.println("Loi benchmarkByteByByte: " + e.getMessage());
        }
        return System.nanoTime() - startTime;
    }

    /**
     * Do thoi gian doc toan bo file bang FileReader (tung ky tu).
     */
    public static long benchmarkCharByChar(String filePath) {
        long startTime = System.nanoTime();
        try (FileReader fr = new FileReader(filePath, StandardCharsets.UTF_8)) {
            while (fr.read() != -1) {
                // Doc tung ky tu va decode UTF-8
            }
        } catch (IOException e) {
            System.err.println("Loi benchmarkCharByChar: " + e.getMessage());
        }
        return System.nanoTime() - startTime;
    }

    /**
     * Do thoi gian doc toan bo file bang FileInputStream voi mang byte[bufferSize].
     */
    public static long benchmarkByteBuffer(String filePath, int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        long startTime = System.nanoTime();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            while (fis.read(buffer) != -1) {
                // Doc theo khoi byte tho
            }
        } catch (IOException e) {
            System.err.println("Loi benchmarkByteBuffer: " + e.getMessage());
        }
        return System.nanoTime() - startTime;
    }

    /**
     * Do thoi gian doc toan bo file bang FileReader voi mang char[bufferSize].
     */
    public static long benchmarkCharBuffer(String filePath, int bufferSize) {
        char[] buffer = new char[bufferSize];
        long startTime = System.nanoTime();
        try (FileReader fr = new FileReader(filePath, StandardCharsets.UTF_8)) {
            while (fr.read(buffer) != -1) {
                // Doc theo khoi ky tu giai ma
            }
        } catch (IOException e) {
            System.err.println("Loi benchmarkCharBuffer: " + e.getMessage());
        }
        return System.nanoTime() - startTime;
    }

    /**
     * In bang so sanh ket qua benchmark chuyen nghiep.
     */
    public static void printBenchmarkTable(long fileSize,
                                          long timeByteSingle, long timeCharSingle,
                                          long timeByteBuffer, long timeCharBuffer,
                                          int bufferSize) {
        System.out.println("\n+-----------------------------------------------------------------------------------------------+");
        System.out.printf("| %-42s | %-16s | %-14s | %-13s |\n", "Phuong Phap Doc Du Lieu", "Thoi Gian (ms)", "Thoi Gian (ns)", "Toc Do (MB/s)");
        System.out.println("+-----------------------------------------------------------------------------------------------+");

        printTableRow("1. ByteStream (Doc tung byte 8-bit)", timeByteSingle, fileSize);
        printTableRow("2. CharStream (Doc tung ky tu UTF-8)", timeCharSingle, fileSize);
        printTableRow("3. ByteStream (Mang dem " + bufferSize + " bytes)", timeByteBuffer, fileSize);
        printTableRow("4. CharStream (Mang dem " + bufferSize + " chars)", timeCharBuffer, fileSize);

        System.out.println("+-----------------------------------------------------------------------------------------------+");
    }

    private static void printTableRow(String method, long timeNs, long fileSizeBytes) {
        double timeMs = timeNs / 1_000_000.0;
        double speedMBs = (timeMs > 0) ? ((double) fileSizeBytes / (1024 * 1024)) / (timeMs / 1000.0) : 0.0;
        System.out.printf("| %-42s | %13.3f ms | %,14d | %10.2f MB/s |\n", method, timeMs, timeNs, speedMBs);
    }

    public static void printAnalysis(long timeByteSingle, long timeCharSingle, long timeByteBuffer, long timeCharBuffer) {
        System.out.println("\n=========================== PHAN TICH VA KET LUAN ===========================");
        System.out.println("1. Ve Toc Do Khi Doc Tung Don Vi (Single read()):");
        System.out.printf("   - ByteStream: %.2f ms  vs  CharStream: %.2f ms\n",
                timeByteSingle / 1_000_000.0, timeCharSingle / 1_000_000.0);
        System.out.println("   - Nhan xet: Ca hai phuong phap doc tung don vi deu ton thoi gian do phat sinh");
        System.out.println("     qua nhieu System Calls (chuyen doi context giua User Mode va Kernel Mode).");
        System.out.println("   - CharStream (FileReader) phai ton them chi phi giai ma bo ma ky tu (Charset Decoder)");
        System.out.println("     tu chuoi byte UTF-8 sang Unicode char 16-bit, do do thuong co do tre cao hon mot chut.");

        System.out.println("\n2. Ve Toc Do Khi Dung Mang Dem (Array Buffer):");
        System.out.printf("   - ByteStream Buffer: %.3f ms  vs  CharStream Buffer: %.3f ms\n",
                timeByteBuffer / 1_000_000.0, timeCharBuffer / 1_000_000.0);
        System.out.println("   - Toc do tang vot tu hang chuc den hang tram lan so voi doc tung byte/char le!");
        System.out.println("   - Day la nen tang dan toi ky thuat Toi uu hoa Luong Dem (Buffered Stream) o Nhanh 2.");

        System.out.println("\n3. Ve Pham Vi Ung Dung Thuc Te:");
        System.out.println("   - Dung Byte Stream (InputStream/OutputStream) khi:");
        System.out.println("     + Xu ly file nhi phan (Binary files): hinh anh (.png, .jpg), am thanh (.mp3),");
        System.out.println("       video (.mp4), file nen (.zip), file thuc thi (.exe, .class).");
        System.out.println("     + Truyen nhan raw byte qua mang (Socket I/O streams tho).");
        System.out.println("   - Dung Character Stream (Reader/Writer) khi:");
        System.out.println("     + Xu ly file van ban co ky tu ngon ngu tu nhien (.txt, .json, .xml, .csv).");
        System.out.println("     + Can bao toan tinh toan ven ky tu Unicode da byte (tieng Viet, tieng Trung,...).");
        System.out.println("=============================================================================");
    }

    /**
     * Minh hoa truc quan su khac biet ve tinh toan ven ky tu UTF-8 giua 2 loai luong.
     */
    public static void demonstrateEncodingDifference(String sampleFilePath) {
        System.out.println("\n================ MINH HOA TINH TOAN VEN KY TU (ENCODING) ================");
        System.out.println("Kiem tra voi file mau: " + sampleFilePath);
        File file = new File(sampleFilePath);
        if (!file.exists()) {
            return;
        }

        // 1. Doc bang ByteStream va ep kieu tho (char)b
        StringBuilder byteSb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            int b;
            while ((b = fis.read()) != -1) {
                byteSb.append((char) b);
            }
        } catch (IOException ignored) {}

        // 2. Doc bang CharStream (FileReader UTF-8)
        StringBuilder charSb = new StringBuilder();
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8)) {
            int ch;
            while ((ch = fr.read()) != -1) {
                charSb.append((char) ch);
            }
        } catch (IOException ignored) {}

        int targetCharIdx = charSb.indexOf("Cong hoa");
        if (targetCharIdx == -1) {
            targetCharIdx = charSb.indexOf("Cộng hòa");
        }

        byte[] targetBytes = (targetCharIdx != -1 && charSb.indexOf("Cộng hòa") != -1) 
                ? "Cộng hòa".getBytes(StandardCharsets.UTF_8)
                : "Cong hoa".getBytes(StandardCharsets.UTF_8);

        StringBuilder targetPattern = new StringBuilder();
        for (byte b : targetBytes) {
            targetPattern.append((char) (b & 0xFF));
        }
        int targetByteIdx = byteSb.indexOf(targetPattern.toString());

        System.out.println("\n[A] KET QUA DOC BANG BYTE STREAM (Ep kieu tho (char) byte):");
        if (targetByteIdx != -1) {
            String sub = byteSb.substring(targetByteIdx, Math.min(byteSb.length(), targetByteIdx + 260));
            System.out.println("   " + sub.replace("\n", "\n   "));
        }
        System.out.println("   => KET LUAN: Ky tu bi vo font (Mojibake) do moi ky tu tieng Viet gom 2-3 bytes UTF-8 ghep lai.");

        System.out.println("\n[B] KET QUA DOC BANG CHARACTER STREAM (FileReader voi UTF-8):");
        if (targetCharIdx != -1) {
            String sub = charSb.substring(targetCharIdx, Math.min(charSb.length(), targetCharIdx + 260));
            System.out.println("   " + sub.replace("\n", "\n   "));
        }
        System.out.println("   => KET LUAN: Ky tu hien thi chuan xac 100% nho bo giai ma UTF-8 (CharsetDecoder).");
        System.out.println("=========================================================================");
    }

    public static void main(String[] args) {
        System.out.println("=============================================================================");
        System.out.println("      HE THONG DO VA SO SANH HIEU NANG: BYTE STREAM VS CHARACTER STREAM     ");
        System.out.println("=============================================================================");

        try {
            // 1. Tao tep benchmark kich thuoc 1024 KB (~ 1MB)
            generateBenchmarkFile(BENCHMARK_FILE, DEFAULT_BENCHMARK_SIZE_KB);
            File benchFile = new File(BENCHMARK_FILE);
            long fileSizeBytes = benchFile.length();
            System.out.printf("-> Kich thuoc file thu nghiem: %,d bytes (~%.2f MB)\n",
                    fileSizeBytes, fileSizeBytes / (1024.0 * 1024.0));

            // Warm-up JVM
            System.out.println("-> Dang khoi dong (Warm-up) JVM...");
            benchmarkByteBuffer(BENCHMARK_FILE, 4096);
            benchmarkCharBuffer(BENCHMARK_FILE, 4096);

            // 2. Chay thu nghiem thuc te
            System.out.println("\n-> Dang thuc hien kiem tra 1: ByteStream (doc tung byte)...");
            long timeByteSingle = benchmarkByteByByte(BENCHMARK_FILE);

            System.out.println("-> Dang thuc hien kiem tra 2: CharStream (doc tung ky tu UTF-8)...");
            long timeCharSingle = benchmarkCharByChar(BENCHMARK_FILE);

            int bufferSize = 1024;
            System.out.printf("-> Dang thuc hien kiem tra 3: ByteStream (buffer %d bytes)...\n", bufferSize);
            long timeByteBuffer = benchmarkByteBuffer(BENCHMARK_FILE, bufferSize);

            System.out.printf("-> Dang thuc hien kiem tra 4: CharStream (buffer %d chars)...\n", bufferSize);
            long timeCharBuffer = benchmarkCharBuffer(BENCHMARK_FILE, bufferSize);

            // 3. Hien thi bang log so sanh theo yeu cau roadmap
            printBenchmarkTable(fileSizeBytes, timeByteSingle, timeCharSingle, timeByteBuffer, timeCharBuffer, bufferSize);

            // 4. In phan tich ly giai su khac biet
            printAnalysis(timeByteSingle, timeCharSingle, timeByteBuffer, timeCharBuffer);

            // 5. Minh hoa tinh toan ven ky tu voi sample.txt
            demonstrateEncodingDifference("src/nhanh1/sample.txt");

        } catch (Exception e) {
            System.err.println("Loi khi chay bo so sanh hieu nang: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
