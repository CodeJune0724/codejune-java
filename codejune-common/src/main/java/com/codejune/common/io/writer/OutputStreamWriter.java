package com.codejune.common.io.writer;

import com.codejune.common.io.Writer;
import java.io.OutputStream;

/**
 * OutputStreamWriter
 *
 * @author ZJ
 * */
public final class OutputStreamWriter extends Writer {

    public OutputStreamWriter(OutputStream outputStream) {
        super(outputStream);
    }

}