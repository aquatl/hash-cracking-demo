package com.acmebreach.attack;

import com.acmebreach.data.User;
import com.acmebreach.hashing.HashUtil;

import java.util.List;


public class SlowHashBenchmark {

    public static final int ITERATIONS = 310_000;

    public static class Result {
        public int sampleSize;
        public long sampleNanos;
        public double hashesPerSecond;
        public long fullAttackAttempts;
        public double estimatedFullAttackSeconds;

        public int sampleCracked;
    }

    public Result benchmarkAndExtrapolate(List<String> keyspace, List<User> targets,
                                           long fullKeyspaceSize,
                                           int sampleUserCount, int sampleCandidateCount) {
        Result result = new Result();

        int userSample = Math.min(sampleUserCount, targets.size());
        int candidateSample = Math.min(sampleCandidateCount, keyspace.size());
        result.sampleSize = userSample * candidateSample;

        long t0 = System.nanoTime();
        int cracked = 0;
        for (int u = 0; u < userSample; u++) {
            User user = targets.get(u);

            String targetHash = HashUtil.pbkdf2Hex(user.plaintextForGradingOnly.toCharArray(),
                    user.salt, ITERATIONS, 256);

            for (int c = 0; c < candidateSample; c++) {
                String candidate = keyspace.get(c);
                String h = HashUtil.pbkdf2Hex(candidate.toCharArray(), user.salt, ITERATIONS, 256);
                if (h.equals(targetHash)) {
                    cracked++;
                    break;
                }
            }
        }
        result.sampleNanos = System.nanoTime() - t0;
        result.sampleCracked = cracked;

        result.hashesPerSecond = result.sampleSize / (result.sampleNanos / 1_000_000_000.0);

        long fullAttempts = (long) targets.size() * fullKeyspaceSize;
        result.fullAttackAttempts = fullAttempts;
        result.estimatedFullAttackSeconds = fullAttempts / result.hashesPerSecond;

        return result;
    }
}
