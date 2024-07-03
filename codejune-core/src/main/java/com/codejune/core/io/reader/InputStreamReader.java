package com.codejune.core.io.reader;

import com.codejune.core.BaseException;
import com.codejune.core.io.Reader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
            byte[] bytes = new byte[this.getReadSize()];
            int size = this.inputStream.read(bytes, 0, this.getReadSize());
            while (size != -1) {
                listener.accept(ByteBuffer.wrap(bytes, 0, size));
                size = this.inputStream.read(bytes, 0, this.getReadSize());
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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.read(byteBuffer -> {
            try {
                byteArrayOutputStream.write(Reader.getByte(byteBuffer));
            } catch (Exception e) {
                throw new BaseException(e);
            }
        });
        return byteArrayOutputStream.toByteArray();
    }

}