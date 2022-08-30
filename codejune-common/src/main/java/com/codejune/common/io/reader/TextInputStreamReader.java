package com.codejune.common.io.reader;

import com.codejune.common.Charset;
import com.codejune.common.Range;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.Reader;
import com.codejune.common.util.IOUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文本输入流读取器
 *
 * @author ZJ
 * */
public final class TextInputStreamReader extends Reader<String> {

    private Charset charset = Charset.UTF_8;

    public TextInputStreamReader(InputStream inputStream) {
        super(inputStream);
    }
    public void setCharset(Charset charset) {
        if (charset == null) {
            return;
        }
        this.charset = charset;
    }

    @Override
    public void read() {
        read(null);
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * */
    public void read(Range range) {
        if (range == null) {
            range = new Range(0, null);
        }
        Integer difference = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (difference != null && difference == 0) {
            return;
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, charset.getName());
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            int lineNum = 0;
            while (line != null) {
                if (range.getEnd() != null && lineNum >= range.getEnd()) {
                    break;
                }
                if (lineNum >= range.getStart()) {
                    readListener.listen(line);
                }
                line = bufferedReader.readLine();
                lineNum = lineNum + 1;
            }
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
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
        textInputStreamReader.setReadListener(data -> {
            result.append(data);
            TextInputStreamReader.this.readListener.listen(data);
        });
        textInputStreamReader.read();
        return result.toString();
    }

}