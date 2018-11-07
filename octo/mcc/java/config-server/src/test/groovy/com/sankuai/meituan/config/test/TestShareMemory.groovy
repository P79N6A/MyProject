package com.sankuai.meituan.config.test

import org.junit.Test

import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

class TestShareMemory {
    @Test
    void testRead() {
        def testMemoryFilePath = "/Users/Jason/Downloads/share_memory/test_memory"
        // 获得一个只读的随机存取文件对象
        RandomAccessFile RAFile = new RandomAccessFile(testMemoryFilePath, "r");
        // 获得相应的文件通道
        FileChannel fc = RAFile.getChannel();
        // 获得共享内存缓冲区
        MappedByteBuffer mapBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, 1024);
        println(readData(mapBuf))
    }

    @Test
    void testWrite() {
        def testMemoryFilePath = "/Users/Jason/Downloads/share_memory/test_memory"
        // 获得一个只读的随机存取文件对象
        RandomAccessFile RAFile = new RandomAccessFile(testMemoryFilePath, "rw");
        // 获得相应的文件通道
        FileChannel fc = RAFile.getChannel();
        // 获得共享内存缓冲区
        MappedByteBuffer mapBuf = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
        //获取文件锁
        FileLock fileLock = fc.tryLock()
        if (fileLock != null) {
            setData(mapBuf, "hello world!")
        } else {
            println("can not get lock")
        }
    }

    static def setData(MappedByteBuffer mappedByteBuffer, String data) {
        def dataBytes = data.bytes
        mappedByteBuffer.put(0, dataBytes.size().byteValue())
        for (i in 1..dataBytes.size()) {
            mappedByteBuffer.put(i, dataBytes[i - 1])
        }
    }

    static def readData(MappedByteBuffer mappedByteBuffer) {
        int length = mappedByteBuffer.get(0)
        byte[] dataBytes = new byte[length]
        for (i in 1..length) {
            dataBytes[i - 1] = mappedByteBuffer.get()
        }
        new String(dataBytes)
    }
}
