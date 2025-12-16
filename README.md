# spsc-ring-buffer
CPU Cache Line Optimized Basic Single Producer Single Consumer Ring Buffer 

# Bencmark

## Hardware And Operating System
 - CPU : AMD Ryzen 5 4600H
 - RAM : 16 GB
 - Windows 10

## Throughput Benchmark
> Using 100 million iterations 

| Test Iterations | Avarage Throughput (ops/sec) |  Avarage Array Blocking Queue Throughput (ops/sec) |
| --------- | ----------------- | - |
|     5     |    29.720.294     | 14.291.348 |

## Latency Benchmark
> Using 50 million iterations

|Queue|Min (ns)| P50 (ns)| Avg (ns) |P99 (ns)|Max (ns)|
|----------|--------|-------|-----|----|----------|
|SPSCRingBuffer| 0 ns     | 100 ns   | 334 ns | 400 ns | 2.682.400 ns |
|ArrayBlockingQueue| 0 ns | 200 ns | 2.043 ns | 6.800 ns | 23.977.900 ns|
