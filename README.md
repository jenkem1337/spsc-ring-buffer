# spsc-ring-buffer
CPU Cache Line Optimized Basic Single Producer Single Consumer Ring Buffer 

# Bencmark

## Hardware And Operating System
 - CPU : AMD Ryzen 5 4600H
 - RAM : 16 GB
 - Windows 10

## Troughput Benchmark
> Using 100 million iterations 

| Iteration | Avarage Troughput (ops/sec) |
| --------- | ----------------- |
|     5     |    29.720.294     |

## Latency Benchmark
> Using 50 million iterations

|Min (ns)| P50 (ns)| Avg (ns) |P99 (ns)|Max (ns)|
|--------|-------|-----|----|----------|
| 0 ns     | 100 ns   | 334 ns | 400 ns      | 2.682.400 ns |
