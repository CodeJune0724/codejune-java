package com.codejune.core.io.reader;

import com.codejune.core.io.Reader;
import java.io.InputStream;

/**
 * 输入流读取器
 *
 * @author ZJ
 * */
public class InputStreamReader extends Reader {

    public InputStreamReader(InputStream inputStream) {
        super(inputStream);
    }

}