package com.acmebreach.attack;

import com.acmebreach.data.User;
import com.acmebreach.hashing.HashUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SaltedBruteForceAttacker {

    public static class Result {
        public final Map<String, String> cracked = new HashMap<>();
        public long crackNanos;
        public long hashOperations;
    }

    public Result attack(List<String> keyspace, List<User> targets) {
        Result result = new Result();
        long t0 = System.nanoTime();

        for (User user : targets) {
            for (String candidate : keyspace) {
                String h = HashUtil.sha256Hex(candidate, user.salt);
                result.hashOperations++;
                if (h.equals(user.sha256Salted)) {
                    result.cracked.put(user.username, candidate);
                    break;
                }
            }
        }

        result.crackNanos = System.nanoTime() - t0;
        return result;
    }
}
