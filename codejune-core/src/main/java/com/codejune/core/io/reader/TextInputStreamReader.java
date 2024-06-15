package com.codejune.core.io.reader;

import com.codejune.core.BaseException;
import com.codejune.core.Range;
import com.codejune.core.io.Reader;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.ObjectUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * 文本输入流读取器
 *
 * @author ZJ
 * */
public final class TextInputStreamReader extends Reader<String> {

    private Charset charset = StandardCharsets.UTF_8;

    public TextInputStreamReader(InputStream inputStream) {
        super(inputStream);
    }

    public void setCharset(Charset charset) {
        if (charset == null) {
            return;
        }
        this.charset = charset;
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * @param listener listener
     * */
    public void read(Range range, Consumer<String> listener) {
        if (range == null) {
            range = new Range(0L, null);
        }
        if (listener == null) {
            listener = s -> {};
        }
        Long length = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (length != null && length == 0) {
            return;
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, charset);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            int lineNum = 0;
            while (line != null) {
                if (range.getEnd() != null && lineNum >= range.getEnd()) {
                    break;
                }
                if (lineNum >= range.getStart()) {
                    listener.accept(line);
                }
                line = bufferedReader.readLine();
                lineNum = lineNum + 1;
            }
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            IOUtil.close(inputStreamReader);
            IOUtil.close(bufferedReader);
        }
    }

    /**
     * 获取文件数据
     *
     * @return 文件数据
     * */
    public String getData() {
        StringBuilder result = new StringBuilder();
        TextInputStreamReader textInputStreamReader = new TextInputStreamReader(this.inputStream);
        textInputStreamReader.read(data -> result.append(data).append("\n"));
        return ObjectUtil.toString(ObjectUtil.subString(result.toString(), result.length() - 1));
    }

    @Override
    public void read(Consumer<String> listener) {
        read(null, listener);
    }

}