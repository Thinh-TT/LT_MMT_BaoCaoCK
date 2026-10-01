package nhanh1;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Demo minh hoa co che hoat dong cua Luong Ky Tu (Character Stream) trong Java.
 * Cac lop dai dien tieu bieu: FileReader va FileWriter.
 *
 * Dac diem:
 * - Xu ly du lieu o muc ky tu Unicode (16-bit, kieu char trong Java).
 * - Tu dong ma hoa (Encoding) va giai ma (Decoding) qua Charset (vi du UTF-8, UTF-16, ISO-8859-1).
 * - Rat toi uu va chuan xac cho cac tep van ban (Text files nhu .txt, .json, .xml, .csv).
 * - Dam bao ky tu da byte (nhu tieng Viet co dau, tieng Nhat, tieng Trung) khong bi vo.
 */
public class CharStreamDemo {

    /**
     * Doc tep tuan tu tung ky tu mot bang FileReader.read().
     *
     * @param filePath duong dan tep van ban can doc
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long readCharByChar(String filePath) {
        System.out.println("\n--- [CharStream] Doc tung ky tu mot (FileReader.read()) ---");
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Tep khong ton tai: " + filePath);
            return -1;
        }

        long charCount = 0;
        StringBuilder preview = new StringBuilder();
        long startTime = System.nanoTime();

        // Su dung UTF-8 ro rang de dam bao tuong thich moi nen tang
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8)) {
            int ch;
            while ((ch = fr.read()) != -1) {
                charCount++;
                if (preview.length() < 350) {
                    preview.append((char) ch);
                }
            }
        } catch (IOException e) {
            System.err.println("Loi doc file CharStream: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Tong so ky tu doc duoc: %,d ky tu\n", charCount);
        System.out.printf("-> Thoi gian thuc thi: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);
        System.out.println("-> Xem truoc (ky tu Unicode duoc giai ma chinh xac):");
        System.out.println("   \"" + preview.toString().replace("\n", "\\n") + "...\"");

        return duration;
    }

    /**
     * Doc tep theo khoi ky tu bang mang buffer char[].
     *
     * @param filePath   duong dan tep can doc
     * @param bufferSize kich thuoc mang ky tu (vi du 1024, 4096 ky tu)
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long readWithBuffer(String filePath, int bufferSize) {
        System.out.printf("\n--- [CharStream] Doc theo khoi char array (kich thuoc %d ky tu) ---\n", bufferSize);
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Tep khong ton tai: " + filePath);
            return -1;
        }

        long charCount = 0;
        char[] buffer = new char[bufferSize];
        long startTime = System.nanoTime();

        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8)) {
            int charsRead;
            while ((charsRead = fr.read(buffer)) != -1) {
                charCount += charsRead;
            }
        } catch (IOException e) {
            System.err.println("Loi doc file CharStream co mang dem: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Tong so ky tu doc duoc: %,d ky tu\n", charCount);
        System.out.printf("-> Thoi gian thuc thi: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);

        return duration;
    }

    /**
     * Ghi van ban ra tep bang FileWriter (chuan ma hoa UTF-8).
     *
     * @param targetPath duong dan tep can ghi
     * @param content    noi dung chuoi can ghi
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long writeString(String targetPath, String content) {
        System.out.println("\n--- [CharStream] Ghi van ban bang FileWriter ---");
        long startTime = System.nanoTime();

        try (FileWriter fw = new FileWriter(targetPath, StandardCharsets.UTF_8)) {
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            System.err.println("Loi ghi file CharStream: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Ghi thanh cong %,d ky tu ra tep: %s\n", content.length(), targetPath);
        System.out.printf("-> Thoi gian ghi: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);

        return duration;
    }

    /**
     * Sao chep tep van ban tu nguon sang dich bang FileReader va FileWriter.
     *
     * @param sourcePath duong dan tep nguon
     * @param destPath   duong dan tep dich
     * @return thoi gian thuc thi (nanosecond)
     */
    public static long copyTextFile(String sourcePath, String destPath) {
        System.out.println("\n--- [CharStream] Sao chep tep van ban bang luong ky tu ---");
        File src = new File(sourcePath);
        if (!src.exists()) {
            System.err.println("Tep nguon khong ton tai: " + sourcePath);
            return -1;
        }

        long startTime = System.nanoTime();
        long totalCharsCopied = 0;

        try (FileReader fr = new FileReader(src, StandardCharsets.UTF_8);
             FileWriter fw = new FileWriter(destPath, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int charsRead;
            while ((charsRead = fr.read(buffer)) != -1) {
                fw.write(buffer, 0, charsRead);
                totalCharsCopied += charsRead;
            }
            fw.flush();
        } catch (IOException e) {
            System.err.println("Loi sao chep tep van ban: " + e.getMessage());
        }

        long duration = System.nanoTime() - startTime;
        System.out.printf("-> Da sao chep %,d ky tu tu [%s] sang [%s]\n", totalCharsCopied, sourcePath, destPath);
        System.out.printf("-> Thoi gian sao chep: %,d ns (%.3f ms)\n", duration, duration / 1_000_000.0);

        return duration;
    }

    /**
     * Doc toan bo tep bang luong ky tu (FileReader UTF-8) va tra ve thoi gian thuc thi tinh bang milliseconds.
     * Phuong thuc nay duoc goi tu GUI Form hoac PerformanceComparator.
     *
     * @param filePath duong dan tep van ban can doc
     * @return thoi gian thuc thi tinh bang milliseconds (ms)
     */
    public static long readAndMeasure(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Tep khong ton tai: " + filePath);
            return -1;
        }

        long charCount = 0;
        long startTime = System.nanoTime();

        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8)) {
            int ch;
            while ((ch = fr.read()) != -1) {
                charCount++;
            }
        } catch (IOException e) {
            System.err.println("Loi doc file CharStream: " + e.getMessage());
            return -1;
        }

        long durationNs = System.nanoTime() - startTime;
        long durationMs = durationNs / 1_000_000L;
        System.out.printf("-> [CharStream] Da doc xong %,d ky tu trong %,d ns (%.3f ms)\n",
                charCount, durationNs, durationNs / 1_000_000.0);

        return durationMs;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" CHUONG TRINH DEMO LUONG KY TU (CHARACTER STREAM) ");
        System.out.println("==================================================");

        String samplePath = "src/nhanh1/sample.txt";
        String copyPath = "src/nhanh1/sample_char_copy.txt";

        // 1. Doc tung ky tu
        readCharByChar(samplePath);

        // 2. Doc theo khoi mang char
        readWithBuffer(samplePath, 1024);

        // 3. Sao chep tep bang char stream
        copyTextFile(samplePath, copyPath);

        System.out.println("\n==> Hoan tat demo CharStreamDemo!");
    }
}
