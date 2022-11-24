package com.codejune.common.io.reader;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.ByteBuffer;
import com.codejune.common.io.Reader;
import java.io.InputStream;
import java.util.Arrays;

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
            byte[] bytes = new byte[this.readSize];
            int size = this.inputStream.read(bytes);
            while (size != -1) {
                readListener.listen(new ByteBuffer(Arrays.copyOf(bytes, size), size));
                size = this.inputStream.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}