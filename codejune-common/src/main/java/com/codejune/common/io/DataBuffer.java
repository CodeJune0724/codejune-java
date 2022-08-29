package com.codejune.common.io;

/**
 * 数据缓冲区
 *
 * @author ZJ
 * */
public class DataBuffer {

    private final byte[] bytes;

    private final int length;

    public DataBuffer(byte[] bytes, int length) {
        this.bytes = bytes;
        this.length = length;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getLength() {
        return length;
    }

}