package com.codejune.core.io.reader;

import com.codejune.core.BaseException;
import com.codejune.core.io.Reader;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 输入流读取器
 *
 * @author ZJ
 * */
public class InputStreamReader extends Reader<ByteBuffer> {

    public InputStreamReader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public final void read(Consumer<ByteBuffer> listener) {
        if (listener == null) {
            listener = byteBuffer -> {};
        }
        try {
            byte[] bytes = new byte[this.size];
            int size = this.inputStream.read(bytes, 0, this.size);
            while (size != -1) {
                listener.accept(ByteBuffer.wrap(bytes, 0, size));
                size = this.inputStream.read(bytes, 0, this.size);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取byte[]
     *
     * @return byte[]
     * */
    public final byte[] getByte() {
        byte[] result = new byte[this.getSize()];
        AtomicInteger index = new AtomicInteger(0);
        this.read(byteBuffer -> {
            byte[] aByte = Reader.getByte(byteBuffer);
            System.arraycopy(aByte, 0, result, index.get(), aByte.length);
            index.set(index.get() + aByte.length);
        });
        return result;
    }

}