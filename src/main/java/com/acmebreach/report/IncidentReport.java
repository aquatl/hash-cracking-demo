package com.acmebreach.report;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncidentReport {

    public static class Row {
        String scenario, defense, crackedSummary, timeSummary, memorySummary, verdict;

        Row(String scenario, String defense, String crackedSummary, String timeSummary,
            String memorySummary, String verdict) {
            this.scenario = scenario;
            this.defense = defense;
            this.crackedSummary = crackedSummary;
            this.timeSummary = timeSummary;
            this.memorySummary = memorySummary;
            this.verdict = verdict;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final int numUsers;
    private final String charset;
    private final int maxLength;
    private final long keyspaceSize;

    public IncidentReport(int numUsers, String charset, int maxLength, long keyspaceSize) {
        this.numUsers = numUsers;
        this.charset = charset;
        this.maxLength = maxLength;
        this.keyspaceSize = keyspaceSize;
    }

    public void addRow(String scenario, String defense, String crackedSummary, String timeSummary,
                        String memorySummary, String verdict) {
        rows.add(new Row(scenario, defense, crackedSummary, timeSummary, memorySummary, verdict));
    }

    public void writeMarkdown(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Báo cáo sự cố: Vụ rò rỉ AcmeCorp\n\n");
        sb.append("**Ngày:** ").append(LocalDateTime.now()).append("  \n");
        sb.append("**Số người dùng bị ảnh hưởng:** ").append(numUsers).append("  \n");
        sb.append("**Không gian brute-force của kẻ tấn công:** charset \"").append(charset)
          .append("\" (").append(charset.length()).append(" ký tự), độ dài tối đa ").append(maxLength)
          .append(" \u2192 ").append(keyspaceSize).append(" khả năng (K)\n\n");

        sb.append("## Kết quả\n\n");
        sb.append("| Tình huống | Cách tấn công | Số người bị bẻ | Thời gian của kẻ tấn công | Bộ nhớ của kẻ tấn công | Đánh giá |\n");
        sb.append("|---|---|---|---|---|---|\n");
        for (Row r : rows) {
            sb.append("| ").append(r.scenario)
              .append(" | ").append(r.defense)
              .append(" | ").append(r.crackedSummary)
              .append(" | ").append(r.timeSummary)
              .append(" | ").append(r.memorySummary)
              .append(" | ").append(r.verdict)
              .append(" |\n");
        }

        // Ghi bằng UTF-8 để tiếng Việt không bị lỗi font trên mọi hệ điều hành.
        try (FileWriter fw = new FileWriter(path, StandardCharsets.UTF_8)) {
            fw.write(sb.toString());
        }
    }
}
