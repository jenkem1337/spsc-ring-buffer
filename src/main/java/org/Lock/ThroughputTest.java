package org.Lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class ThroughputTest {
    public static void main(String[] args) throws InterruptedException {
        var buffer = new SPSCRingBuffer<Integer>((int) Math.pow(2,16));
        var latch = new CountDownLatch(2);
        final var total = 100_000_000;
        long[] producerTime = new long[1];
        long[] consumerTime = new long[1];

        Thread producer = new Thread(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            long start = System.nanoTime();

            for (int i = 0; i < total; ) {
                boolean offered = buffer.offer(i);
                if (!offered) {
                    Thread.onSpinWait();
//                    LockSupport.parkNanos(1);
                    continue;
                }
                i++;
            }

            long end = System.nanoTime();
            producerTime[0] = end - start;
            latch.countDown();
        });

        Thread consumer = new Thread(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            long start = System.nanoTime();

            for (int i = 0; i < total; ) {
                Integer value = buffer.poll();
                if (value == null) {
                    Thread.onSpinWait();
                    continue;
                }

                i++;
            }

            long end = System.nanoTime();
            consumerTime[0] = end - start;
            latch.countDown();
        });

        long globalStart = System.nanoTime();
        producer.start();
        consumer.start();
        latch.await();
        long globalEnd = System.nanoTime();

        long producerMs = producerTime[0] / 1_000_000;
        long consumerMs = consumerTime[0] / 1_000_000;
        long totalMs = (globalEnd - globalStart) / 1_000_000;

        double producerThroughput = total / (producerTime[0] / 1_000_000_000.0);
        double consumerThroughput = total / (consumerTime[0] / 1_000_000_000.0);
        double systemThroughput = total / (totalMs / 1000.0);

        System.out.println("=== Throughput Test Results ===");
        System.out.println("Producer time (ms): " + producerMs);
        System.out.println("Consumer time (ms): " + consumerMs);
        System.out.println("Total time (ms): " + totalMs);
        System.out.printf("Producer throughput: %.2f ops/sec%n", producerThroughput);
        System.out.printf("Consumer throughput: %.2f ops/sec%n", consumerThroughput);
        System.out.printf("System throughput: %.2f ops/sec%n", systemThroughput);

    }
}