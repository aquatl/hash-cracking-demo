package com.acmebreach.data;

import com.acmebreach.bruteforce.Keyspace;
import com.acmebreach.hashing.HashUtil;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Tạo bảng người dùng của một công ty hư cấu
 */
public final class UserDatabase {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_BYTES = 16;

    // Ký tự dùng cho mật khẩu nằm ngoài không gian brute-force
    private static final String OUTSIDE_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    private UserDatabase() {
    }

    public static List<User> generate(int numUsers, Keyspace keyspace, double secureUserFraction) {
        List<User> users = new ArrayList<>(numUsers);
        int secureCount = (int) Math.round(numUsers * secureUserFraction);

        for (int i = 0; i < numUsers; i++) {
            String username = "user" + String.format("%05d", i);
            boolean secure = i < secureCount;

            String plaintext = secure
                    ? generateOutsidePassword(keyspace.maxLength())
                    : keyspace.randomInside(RNG);

            String saltHex = HashUtil.randomSaltHex(SALT_BYTES);

            String sha1NoSalt = HashUtil.sha1Hex(plaintext);
            String sha256NoSalt = HashUtil.sha256Hex(plaintext);
            String sha256Salted = HashUtil.sha256Hex(plaintext, saltHex);

            users.add(new User(username, plaintext, !secure, sha1NoSalt, sha256NoSalt, saltHex, sha256Salted));
        }

        // Xáo trộn
        java.util.Collections.shuffle(users, RNG);
        return users;
    }

    /** Mật khẩu chắc chắn nằm ngoài không gian brute-force: dài hơn maxLength ít nhất 2 ký tự,
     *  và lấy từ một bộ ký tự rộng hơn */
    private static String generateOutsidePassword(int maxLength) {
        int length = maxLength + 2 + RNG.nextInt(5); // maxLength+2 .. maxLength+6
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(OUTSIDE_CHARS.charAt(RNG.nextInt(OUTSIDE_CHARS.length())));
        }
        return sb.toString();
    }
}
