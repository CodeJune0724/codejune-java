package com.codejune.common.io.reader;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.Reader;
import java.io.InputStream;
import java.nio.ByteBuffer;

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
    public final void read() {
        try {
            byte[] bytes = new byte[this.size];
            int size = this.inputStream.read(bytes);
            while (size != -1) {
                listener.listen(ByteBuffer.wrap(bytes, 0, size));
                size = this.inputStream.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}