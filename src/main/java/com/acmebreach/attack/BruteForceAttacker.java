package com.acmebreach.attack;

import com.acmebreach.data.User;
import com.acmebreach.hashing.HashUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BruteForceAttacker {

    public static class Result {
        public final Map<String, String> cracked = new HashMap<>(); // username -> mật khẩu tìm được
        public long buildMapNanos;
        public long crackNanos;
        public long hashOperations; // = kích thước không gian mật khẩu (mỗi chuỗi băm đúng 1 lần)
    }

    private final HashUtil.HashFn hashFn;

    public BruteForceAttacker(HashUtil.HashFn hashFn) {
        this.hashFn = hashFn;
    }

    /**
     * @param keyspace       toàn bộ không gian mật khẩu đã liệt kê (xem {@code Keyspace.generateAll()})
     * @param targets        người dùng cần bẻ
     * @param targetHashOf   lấy đúng cột hash (không salt) cần tấn công của một User
     */
    public Result attack(List<String> keyspace, List<User> targets, Function<User, String> targetHashOf) {
        Result result = new Result();

        long t0 = System.nanoTime();
        Map<String, String> hashToPlain = new HashMap<>(keyspace.size() * 2);
        for (String candidate : keyspace) {
            hashToPlain.put(hashFn.hash(candidate), candidate);
        }
        result.hashOperations = keyspace.size();
        result.buildMapNanos = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        for (User user : targets) {
            String guess = hashToPlain.get(targetHashOf.apply(user));
            if (guess != null) {
                result.cracked.put(user.username, guess);
            }
        }
        result.crackNanos = System.nanoTime() - t1;

        return result;
    }
}
