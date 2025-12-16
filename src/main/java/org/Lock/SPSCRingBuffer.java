package org.Lock;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class SPSCRingBuffer<E> {
    private final E[] buffer;

    private long p1,p2,p3,p4,p5,p6,p7;
    private volatile long  readIndex;
    private long p8,p9,p10,p11,p12,p13,p14;
    private volatile long writeIndex;
    private long p15,p16,p17,p18,p19,p20,p21;

    private long writeIndexCache;
    private long readIndexCache;
    private final int mask;

    private static final VarHandle BUFFER_VH;
    private static final VarHandle WRITE_INDEX_VH;
    private static final VarHandle READ_INDEX_VH;

    public SPSCRingBuffer(int capacity) {
        if(Integer.bitCount(capacity) != 1) {
            throw new IllegalArgumentException("Capacity must be a power of 2");
        }
        this.buffer =  (E[]) new Object[capacity];
        this.mask = capacity - 1;
        this.writeIndex = 0;
        this.readIndex = 0;
    }
    static {
        try {
            BUFFER_VH = MethodHandles.arrayElementVarHandle(Object[].class);
            WRITE_INDEX_VH = MethodHandles.lookup().findVarHandle(SPSCRingBuffer.class, "writeIndex", long.class);
            READ_INDEX_VH = MethodHandles.lookup().findVarHandle(SPSCRingBuffer.class, "readIndex", long.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
    public boolean offer(E element){
        long _writeIndex = (long) WRITE_INDEX_VH.getOpaque(this);
        long _nextWriteIndex = (_writeIndex + 1) & mask;
        if(_nextWriteIndex == readIndexCache) {
            readIndexCache = (long) READ_INDEX_VH.getAcquire(this);
            if(_nextWriteIndex == readIndexCache) {
                return false;
            }
        }
        BUFFER_VH.setRelease(buffer, (int) _writeIndex, element);
        WRITE_INDEX_VH.setRelease(this, _nextWriteIndex);
        return true;
    }
    public E poll(){
        long _readIndex = (long) READ_INDEX_VH.getOpaque(this);
        if(_readIndex == writeIndexCache) {
            writeIndexCache = (long) WRITE_INDEX_VH.getAcquire(this);
            if(_readIndex == writeIndexCache) {
                return null;
            }
        }
        E element = (E) BUFFER_VH.getAcquire(buffer, (int)_readIndex);
        long nextReadIndex = (_readIndex + 1) & mask;
//        BUFFER_VH.setRelease(buffer, (int) nextReadIndex, null);
        READ_INDEX_VH.setRelease(this, nextReadIndex);
        return element;
    }
}
