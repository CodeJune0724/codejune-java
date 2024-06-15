package com.codejune.core.io.writer;

import com.codejune.core.io.Writer;
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