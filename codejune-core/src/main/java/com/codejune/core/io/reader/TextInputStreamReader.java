package com.codejune.core.io.reader;

import com.codejune.core.BaseException;
import com.codejune.core.Range;
import com.codejune.core.io.Reader;
import com.codejune.core.util.ObjectUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * 文本输入流读取器
 *
 * @author ZJ
 * */
public final class TextInputStreamReader extends Reader {

    private final Charset charset;

    public TextInputStreamReader(InputStream inputStream, Charset charset) {
        super(inputStream);
        this.charset = charset;
    }

    public TextInputStreamReader(InputStream inputStream) {
        this(inputStream, null);
    }

    /**
     * 读取行
     *
     * @param consumer consumer
     * @param range 读取范围
     * */
    public void readLine(Consumer<String> consumer, Range range) {
        if (range == null) {
            range = new Range(0L, null);
        }
        if (consumer == null) {
            consumer = _ -> {};
        }
        Long length = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (length != null && length <= 0) {
            return;
        }
        try {
            BufferedReader bufferedReader;
            if (this.charset == null) {
                bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream, this.charset));
            }
            String line = bufferedReader.readLine();
            int lineNum = 0;
            while (line != null) {
                if (range.getEnd() != null && lineNum >= range.getEnd()) {
                    break;
                }
                if (lineNum >= range.getStart()) {
                    consumer.accept(line);
                }
                line = bufferedReader.readLine();
                lineNum = lineNum + 1;
            }
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 读取行
     *
     * @param consumer consumer
     * */
    public void readLine(Consumer<String> consumer) {
        this.readLine(consumer, null);
    }

    /**
     * 获取文件数据
     *
     * @return 文件数据
     * */
    public String getData() {
        StringBuilder result = new StringBuilder();
        this.readLine(line -> result.append(line).append("\n"));
        return ObjectUtil.toString(ObjectUtil.subString(result.toString(), result.length() - 1));
    }

}