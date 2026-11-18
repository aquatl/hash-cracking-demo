package com.acmebreach.hashing;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.nio.charset.StandardCharsets;

/**
 * Tất cả các hàm băm cơ bản dùng trong demo.
 */
public final class HashUtil {

    private static final SecureRandom RNG = new SecureRandom();

    private static final ThreadLocal<MessageDigest> SHA1 = ThreadLocal.withInitial(() -> getDigest("SHA-1"));
    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> getDigest("SHA-256"));

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private HashUtil() {
    }


    @FunctionalInterface
    public interface HashFn {
        String hash(String input);
    }


    public static String sha1Hex(String input) {
        byte[] out = SHA1.get().digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(out);
    }

    public static String sha1Hex(String input, String saltHex) {
        return sha1Hex(saltHex + ":" + input);
    }


    public static String sha256Hex(String input) {
        byte[] out = SHA256.get().digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(out);
    }

    public static String sha256Hex(String input, String saltHex) {
        return sha256Hex(saltHex + ":" + input);
    }

    public static String pbkdf2Hex(char[] password, String saltHex, int iterations, int keyLengthBits) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] salt = hexToBytes(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }


    public static String randomSaltHex(int numBytes) {
        byte[] salt = new byte[numBytes];
        RNG.nextBytes(salt);
        return bytesToHex(salt);
    }

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = HEX_DIGITS[v >>> 4];
            out[i * 2 + 1] = HEX_DIGITS[v & 0x0F];
        }
        return new String(out);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }
}
