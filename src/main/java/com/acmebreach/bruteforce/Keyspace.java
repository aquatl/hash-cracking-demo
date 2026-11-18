package com.acmebreach.bruteforce;

import java.util.ArrayList;
import java.util.List;

/**
 * "Không gian mật khẩu" (keyspace) mà kẻ tấn công brute-force sẽ duyệt hết.
 */
public final class Keyspace {

    private final String charset;
    private final int maxLength;

    public Keyspace(String charset, int maxLength) {
        if (charset.isEmpty()) throw new IllegalArgumentException("charset rỗng");
        if (maxLength < 1) throw new IllegalArgumentException("maxLength phải >= 1");
        this.charset = charset;
        this.maxLength = maxLength;
    }

    public String charset() {
        return charset;
    }

    public int maxLength() {
        return maxLength;
    }

    /** Kích thước không gian mật khẩu */
    public long size() {
        long total = 0;
        long power = 1;
        long base = charset.length();
        for (int len = 1; len <= maxLength; len++) {
            power *= base;
            total += power;
        }
        return total;
    }

    /**
     * Liệt kê all chuỗi trong không gian mật khẩu
     */
    public List<String> generateAll() {
        long size = size();
        if (size > 50_000_000L) {
            throw new IllegalStateException("Không gian mật khẩu quá lớn để brute-force trong demo này: "
                    + size + " khả năng. Hãy giảm --max-length hoặc dùng charset nhỏ hơn.");
        }
        List<String> all = new ArrayList<>((int) size);
        char[] chars = charset.toCharArray();
        int base = chars.length;
        for (int len = 1; len <= maxLength; len++) {
            long count = 1;
            for (int i = 0; i < len; i++) count *= base;
            char[] buffer = new char[len];
            for (long idx = 0; idx < count; idx++) {
                long n = idx;
                for (int pos = len - 1; pos >= 0; pos--) {
                    buffer[pos] = chars[(int) (n % base)];
                    n /= base;
                }
                all.add(new String(buffer));
            }
        }
        return all;
    }

    /** Một chuỗi ngẫu nhiên */
    public String randomInside(java.util.Random rng) {
        int len = 1 + rng.nextInt(maxLength);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(charset.charAt(rng.nextInt(charset.length())));
        }
        return sb.toString();
    }
}
