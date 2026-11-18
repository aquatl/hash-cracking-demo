package com.acmebreach.report;

public final class Format {

    private Format() {
    }

    public static String nanos(long nanos) {
        return seconds(nanos / 1_000_000_000.0);
    }

    /** Định dạng thời gian (tính bằng giây) */
    public static String seconds(double secs) {
        if (secs < 0.000001) return String.format("%.0f ns", secs * 1_000_000_000);
        if (secs < 0.001) return String.format("%.2f \u00b5s", secs * 1_000_000);
        if (secs < 1) return String.format("%.2f ms", secs * 1000);
        if (secs < 60) return String.format("%.2f giây", secs);
        if (secs < 3600) return String.format("%.1f phút", secs / 60);
        if (secs < 86400) return String.format("%.1f giờ", secs / 3600);
        if (secs < 31_536_000.0) return String.format("%.1f ngày", secs / 86400);
        double years = secs / 31_536_000.0;
        if (years < 1000) return String.format("%.1f năm", years);
        if (years < 1_000_000) return String.format("%.1f nghìn năm", years / 1000);
        if (years < 1_000_000_000) return String.format("%.1f triệu năm", years / 1_000_000);
        return String.format("%.2e năm", years);
    }

    public static String bytes(long b) {
        if (b < 1024) return b + " B";
        if (b < 1024 * 1024) return String.format("%.1f KB", b / 1024.0);
        if (b < 1024L * 1024 * 1024) return String.format("%.1f MB", b / (1024.0 * 1024));
        return String.format("%.2f GB", b / (1024.0 * 1024 * 1024));
    }

    public static String pct(int part, int whole) {
        if (whole == 0) return "0.0%";
        return String.format("%.1f%%", 100.0 * part / whole);
    }

    public static String bar(int part, int whole, int width) {
        int filled = whole == 0 ? 0 : (int) Math.round(width * (double) part / whole);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? '#' : '-');
        }
        sb.append(']');
        return sb.toString();
    }
}
