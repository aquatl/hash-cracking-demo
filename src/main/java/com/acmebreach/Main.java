package com.acmebreach;

import com.acmebreach.attack.BruteForceAttacker;
import com.acmebreach.attack.SaltedBruteForceAttacker;
import com.acmebreach.attack.SlowHashBenchmark;
import com.acmebreach.bruteforce.Keyspace;
import com.acmebreach.data.User;
import com.acmebreach.data.UserDatabase;
import com.acmebreach.hashing.HashUtil;
import com.acmebreach.report.Format;
import com.acmebreach.report.IncidentReport;
import com.acmebreach.story.Narrator;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class Main {

    public static void main(String[] args) throws Exception {
        // In bằng UTF-8 để tiếng Việt hiện đúng
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        int numUsers = 300;
        int maxLength = 3;
        String charset = "abcdefghijklmnopqrstuvwxyz0123456789";
        double secureFraction = 0.10;
        boolean fast = false;
        String reportPath = "INCIDENT_REPORT.md";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--users":
                    numUsers = Integer.parseInt(args[++i]);
                    break;
                case "--max-length":
                    maxLength = Integer.parseInt(args[++i]);
                    break;
                case "--charset":
                    charset = args[++i];
                    break;
                case "--secure-fraction":
                    secureFraction = Double.parseDouble(args[++i]);
                    break;
                case "--fast":
                    fast = true;
                    break;
                case "--report":
                    reportPath = args[++i];
                    break;
                case "--help":
                    printHelp();
                    return;
                default:
                    System.out.println("Tham số không hợp lệ: " + args[i]);
                    printHelp();
                    return;
            }
        }

        Keyspace keyspace;
        long keyspaceSize;
        try {
            keyspace = new Keyspace(charset, maxLength);
            keyspaceSize = keyspace.size();
            if (keyspaceSize > 50_000_000L) {
                throw new IllegalStateException("Không gian mật khẩu quá lớn để brute-force trong demo này: "
                        + keyspaceSize + " khả năng.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Lỗi cấu hình: " + e.getMessage());
            System.out.println("Hãy giảm --max-length hoặc dùng --charset nhỏ hơn. Chạy --help để xem chi tiết.");
            return;
        }

        Narrator n = new Narrator(fast);
        IncidentReport report = new IncidentReport(numUsers, charset, maxLength, keyspaceSize);

        n.scene("VỤ RÒ RỈ ACMECORP Ngày 0");
        n.line("Cơ sở dữ liệu người dùng của AcmeCorp vừa bị lấy cắp.");
        n.line("Số người dùng bị ảnh hưởng (đã thu nhỏ cho demo): " + numUsers);
        n.line("Không gian mật khẩu brute-force: charset \"" + charset + "\" (" + charset.length()
                + " ký tự), độ dài tối đa " + maxLength + " \u2192 " + keyspaceSize + " khả năng.");

        List<String> allCandidates = keyspace.generateAll();
        n.line("Đã liệt kê toàn bộ " + allCandidates.size() + " chuỗi trong không gian mật khẩu.");

        n.line("Đang dựng bảng người dùng của AcmeCorp theo các cách băm mật khẩu từng dùng trước đây...");
        List<User> users = UserDatabase.generate(numUsers, keyspace, secureFraction);
        long outsideCount = users.stream().filter(u -> !u.insideBruteForceSpace).count();
        n.line("Bảng người dùng đã sẵn sàng. " + outsideCount + " / " + numUsers
                + " người dùng có mật khẩu NẰM NGOÀI không gian brute-force (không bao giờ bị bẻ ở demo này).");


        // MP 1: SHA-1, không salt, brute-force + bảng tính sẵn

        n.scene("Mô phỏng 1 SHA-1, không salt");
        n.line("AcmeCorp băm mật khẩu bằng SHA-1 thường. Không có salt.");
        n.line("Kẻ tấn công băm TOÀN BỘ không gian mật khẩu một lần, lưu vào bảng sha1(chuỗi) -> chuỗi...");

        BruteForceAttacker sha1Attacker = new BruteForceAttacker(HashUtil::sha1Hex);
        BruteForceAttacker.Result r1 = sha1Attacker.attack(allCandidates, users, u -> u.sha1NoSalt);

        n.line("Dựng xong bảng trong " + Format.nanos(r1.buildMapNanos)
                + " (" + r1.hashOperations + " lần băm \u2014 đúng bằng kích thước không gian mật khẩu).");
        n.line("Giờ crack cả " + numUsers + " người dùng chỉ cần " + numUsers + " lần tra bảng...");
        n.line("Xong trong " + Format.nanos(r1.crackNanos) + ".");
        n.line("Đã crack được " + r1.cracked.size() + " / " + numUsers + " người dùng ("
                + Format.pct(r1.cracked.size(), numUsers) + ") "
                + Format.bar(r1.cracked.size(), numUsers, 30));

        long tableMemory = estimateTableMemoryBytes(keyspaceSize);
        report.addRow("1. SHA-1, không salt", "Brute-force + bảng băm tính sẵn (dùng chung)",
                r1.cracked.size() + " / " + numUsers + " (" + Format.pct(r1.cracked.size(), numUsers) + ")",
                Format.nanos(r1.buildMapNanos + r1.crackNanos),
                Format.bytes(tableMemory) + " (bảng O(K))",
                "Tồi tệ. Có hàng trăm nghìn mật khẩu cũng crack trong nháy mắt");


        n.scene("Mô phỏng 2 SHA-256, không salt");
        n.line("AcmeCorp \"nâng cấp\" sang SHA-256, vẫn chưa thêm salt.");

        BruteForceAttacker sha256Attacker = new BruteForceAttacker(HashUtil::sha256Hex);
        BruteForceAttacker.Result r2 = sha256Attacker.attack(allCandidates, users, u -> u.sha256NoSalt);

        n.line("Dựng xong bảng trong " + Format.nanos(r2.buildMapNanos)
                + " (" + r2.hashOperations + " lần băm).");
        n.line("Xong trong " + Format.nanos(r2.crackNanos) + ".");
        n.line("Đã crack được " + r2.cracked.size() + " / " + numUsers + " người dùng ("
                + Format.pct(r2.cracked.size(), numUsers) + ") "
                + Format.bar(r2.cracked.size(), numUsers, 30));

        report.addRow("2. SHA-256, không salt", "Brute-force + bảng băm tính sẵn (dùng chung)",
                r2.cracked.size() + " / " + numUsers + " (" + Format.pct(r2.cracked.size(), numUsers) + ")",
                Format.nanos(r2.buildMapNanos + r2.crackNanos),
                Format.bytes(tableMemory) + " (bảng O(K))",
                "Vẫn giống mô phỏng 1");

        n.scene("Mô phỏng 3 AcmeCorp thêm salt");

        SaltedBruteForceAttacker salted = new SaltedBruteForceAttacker();
        SaltedBruteForceAttacker.Result r3 = salted.attack(allCandidates, users);

        n.line("Xong trong " + Format.nanos(r3.crackNanos)
                + " (" + r3.hashOperations + " lần băm \u2014 tối đa " + numUsers + " người dùng x "
                + keyspaceSize + " khả năng, dừng sớm khi tìm thấy).");
        n.line("Đã crack được " + r3.cracked.size() + " / " + numUsers + " người dùng ("
                + Format.pct(r3.cracked.size(), numUsers) + ") "
                + Format.bar(r3.cracked.size(), numUsers, 30));
        n.line("Những người KHÔNG bị bẻ đều là người có mật khẩu nằm ngoài không gian brute-force \u2014");
        n.line("salt không làm một mật khẩu yếu khó đoán hơn, nó chỉ ngăn kẻ tấn công dùng lại một bảng cho mọi người.");

        report.addRow("3. SHA-256, có salt", "Brute-force riêng cho từng người (không dùng lại được bảng tính sẵn)",
                r3.cracked.size() + " / " + numUsers + " (" + Format.pct(r3.cracked.size(), numUsers) + ")",
                Format.nanos(r3.crackNanos),
                "không đáng kể (không lưu bảng nào, O(1))",
                "Mật khẩu trong không gian brute-force vẫn bị bẻ; chi phí tăng theo numUsers x K, không đủ lớn");


        n.scene("Mô phỏng 4: Cuối cùng AcmeCorp cũng đọc OWASP Cheat Sheet");
        n.line("Cách băm mới: PBKDF2-HMAC-SHA256, có salt, " + SlowHashBenchmark.ITERATIONS + " vòng lặp.");
        n.line("Mỗi lần đoán giờ cố tình rất tốn công. Chương trình đo tốc độ brute-force THẬT của máy này");
        n.line("trên một mẫu nhỏ, rồi ngoại suy cho cuộc tấn công đầy đủ...");

        SlowHashBenchmark slow = new SlowHashBenchmark();
        int sampleUsers = Math.min(6, numUsers);
        int sampleCandidates = Math.min(12, allCandidates.size());
        SlowHashBenchmark.Result r4 = slow.benchmarkAndExtrapolate(allCandidates, users, keyspaceSize,
                sampleUsers, sampleCandidates);

        n.line("Mẫu: " + sampleUsers + " người dùng x " + sampleCandidates + " lần đoán = " + r4.sampleSize
                + " hash PBKDF2 thật, tính trong " + Format.nanos(r4.sampleNanos) + ".");
        n.line(String.format("Tốc độ brute-force đo được: %.1f hash/giây trên máy này (so với hàng triệu hash/giây của SHA-256).",
                r4.hashesPerSecond));
        n.line("Ngoại suy cho cuộc tấn công đầy đủ: " + numUsers + " người dùng x " + keyspaceSize
                + " khả năng = " + r4.fullAttackAttempts + " lần băm (trường hợp xấu nhất, không tìm thấy sớm).");
        n.line("Thời gian ước tính để brute-force HẾT không gian mật khẩu cho MỌI người dùng: "
                + Format.seconds(r4.estimatedFullAttackSeconds) + ".");
        n.line("(So sánh: ở Mô phỏng 1, cuộc tấn công tương đương vào toàn bộ cơ sở dữ liệu chỉ mất "
                + Format.nanos(r1.buildMapNanos + r1.crackNanos) + ".)");

        report.addRow("4. PBKDF2 (" + SlowHashBenchmark.ITERATIONS + " vòng), có salt",
                "Brute-force riêng cho từng người, đo mẫu thật rồi ngoại suy",
                "chỉ là ước tính \u2014 không chạy hết",
                "ước tính " + Format.seconds(r4.estimatedFullAttackSeconds) + " để brute-force hết với mọi người dùng",
                "không đáng kể (không lưu bảng nào, O(1))",
                "Tấn công trở nên bất khả thi khi quy mô lớn");

        
        n.scene("KẾT");
        n.line("Đang ghi báo cáo sự cố vào " + reportPath + "...");
        report.writeMarkdown(reportPath);
        n.line("Đã ghi báo cáo.");
        n.blank();
        n.raw("Tóm tắt: CÙNG một cơ sở dữ liệu " + numUsers + " người dùng, từ \"bẻ hết trong "
                + Format.nanos(r1.buildMapNanos + r1.crackNanos)
                + "\" thành \"ước tính " + Format.seconds(r4.estimatedFullAttackSeconds)
                + " để brute-force hết\" \u2014 vẫn đúng những mật khẩu đó, chỉ khác cách băm.");
        n.raw("Khoảng cách đó chính là lý do các thuật toán băm mật khẩu chậm (PBKDF2/bcrypt/scrypt/Argon2) tồn tại.");
    }

    /** Ước lượng thô bộ nhớ bảng băm tính sẵn: ~96 byte mỗi mục (hash hex + chuỗi + phần thừa của HashMap). */
    private static long estimateTableMemoryBytes(long entries) {
        return entries * 96L;
    }

    private static void printHelp() {
        System.out.println("Cách dùng: java -jar acmebreach.jar [tùy chọn]");
        System.out.println("  --users N              số người dùng giả lập (mặc định 300)");
        System.out.println("  --max-length N         độ dài tối đa của không gian brute-force (mặc định 3)");
        System.out.println("  --charset STR          bộ ký tự brute-force (mặc định chữ thường + số, 36 ký tự)");
        System.out.println("  --secure-fraction D    tỉ lệ người dùng có mật khẩu NẰM NGOÀI không gian brute-force (mặc định 0.10)");
        System.out.println("  --fast                 bỏ các khoảng dừng kịch tính");
        System.out.println("  --report PATH          nơi ghi báo cáo sự cố markdown (mặc định INCIDENT_REPORT.md)");
        System.out.println();
        System.out.println("Lưu ý: kích thước không gian mật khẩu K = |charset| + |charset|^2 + ... + |charset|^maxLength");
        System.out.println("tăng theo hàm MŨ. Tăng --max-length hoặc --charset quá lớn sẽ khiến demo chạy rất lâu");
        System.out.println("(nhất là Mô phỏng 3). Chương trình sẽ từ chối chạy nếu K vượt quá 50 triệu.");
    }
}
