package org.Lock;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class LatencyTest {
    private static void latencyTest() throws InterruptedException {
        var buffer = new SPSCRingBuffer<Long>((int) Math.pow(2, 16));
        final int warmupRounds = 100_000;
        final int testRounds = 50_000_000;

        System.out.println("Warming up...");
        for (int w = 0; w < 10; w++) {
            runLatencyRound(buffer, warmupRounds);
        }

        System.out.println("Running latency test...");
        long[] latencies = runLatencyRound(buffer, testRounds);

        Arrays.sort(latencies);
        long min = latencies[0];
        long max = latencies[latencies.length - 1];
        double avg = Arrays.stream(latencies).average().orElse(0);
        long p50 = latencies[latencies.length / 2];
        long p90 = latencies[(int) (latencies.length * 0.9)];
        long p99 = latencies[(int) (latencies.length * 0.99)];
        long p999 = latencies[(int) (latencies.length * 0.999)];

        System.out.printf("Latency Results (nanoseconds):%n");
        System.out.printf("  Min: %d ns (%.2f μs)%n", min, min / 1000.0);
        System.out.printf("  Avg: %.2f ns (%.2f μs)%n", avg, avg / 1000.0);
        System.out.printf("  P50: %d ns (%.2f μs)%n", p50, p50 / 1000.0);
        System.out.printf("  P90: %d ns (%.2f μs)%n", p90, p90 / 1000.0);
        System.out.printf("  P99: %d ns (%.2f μs)%n", p99, p99 / 1000.0);
        System.out.printf("  P99.9: %d ns (%.2f μs)%n", p999, p999 / 1000.0);
        System.out.printf("  Max: %d ns (%.2f μs)%n", max, max / 1000.0);
    }

    private static long[] runLatencyRound(SPSCRingBuffer<Long> buffer, int rounds) throws InterruptedException {
        long[] latencies = new long[rounds];
        CountDownLatch latch = new CountDownLatch(2);

        Thread producer = new Thread(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            try {
                long next = System.nanoTime();
                for (int i = 0; i < rounds; i++) {
                    long timestamp = System.nanoTime();

                    while (!buffer.offer(timestamp)) {
                        Thread.onSpinWait();
                        //LockSupport.parkNanos(1);
                    }

                    next += 1000;
                    while (System.nanoTime() < next) {
                        Thread.onSpinWait();
                    }
                }
            } finally {
                latch.countDown();
            }
        });

        Thread consumer = new Thread(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            try {
                for (int i = 0; i < rounds; i++) {
                    Long timestamp;
                    while ((timestamp = buffer.poll()) == null) {
                        Thread.onSpinWait();
                    }
                    long now = System.nanoTime();
                    latencies[i] = now - timestamp;
                }
            } finally {
                latch.countDown();
            }
        });

        producer.start();
        consumer.start();
        latch.await();
        return latencies;
    }

    public static void main(String[] args) throws InterruptedException {
        latencyTest();
    }

}
