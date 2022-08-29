package com.codejune.common.io.reader;

import com.codejune.common.exception.InfoException;
import com.codejune.common.io.DataBuffer;
import com.codejune.common.io.Reader;
import java.io.InputStream;

/**
 * 输入流读取器
 *
 * @author ZJ
 * */
public class InputStreamReader extends Reader<DataBuffer> {

    public InputStreamReader(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public void read() {
        try {
            byte[] bytes = new byte[this.readSize];
            int size = this.inputStream.read(bytes);
            while (size != -1) {
                readListener.listen(new DataBuffer(bytes, size));
                size = this.inputStream.read(bytes);
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}