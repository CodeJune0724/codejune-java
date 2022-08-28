package com.codejune.common.io;

import com.codejune.common.exception.InfoException;
import java.io.InputStream;

/**
 * AbstractReader
 *
 * @author ZJ
 * */
public abstract class AbstractReader {

    protected final InputStream inputStream;

    public AbstractReader(InputStream inputStream) {
        if (inputStream == null) {
            throw new InfoException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

}