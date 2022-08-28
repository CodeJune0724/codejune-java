package com.codejune.common.io.reader;

import com.codejune.common.Charset;
import com.codejune.common.Progress;
import com.codejune.common.Range;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.AbstractReader;
import com.codejune.common.listener.ProgressListener;
import com.codejune.common.listener.TextInputStreamReadListener;
import com.codejune.common.util.IOUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文本输入流读取器
 *
 * @author ZJ
 * */
public final class TextInputStreamReader extends AbstractReader {

    private final InputStream inputStream;

    private Charset charset = Charset.UTF_8;

    public TextInputStreamReader(InputStream inputStream) {
        if (inputStream == null) {
            throw new InfoException("inputStream is null");
        }
        this.inputStream = inputStream;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * 读取
     *
     * @param range 读取范围
     * @param textInputStreamReadListener textInputStreamReadListener
     * @param progressListener progressListener
     * */
    public void read(Range range, TextInputStreamReadListener textInputStreamReadListener, ProgressListener progressListener) {
        if (range == null) {
            range = new Range(0, null);
        }
        Integer difference = range.getEnd() == null ? null : range.getEnd() - range.getStart();
        if (difference != null && difference == 0) {
            return;
        }
        if (textInputStreamReadListener == null) {
            textInputStreamReadListener = data -> {};
        }
        if (progressListener == null) {
            progressListener = data -> {};
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        ProgressListener finalProgressListener = progressListener;
        try {
            Progress progress = new Progress(difference == null ? this.inputStream.available() : difference) {
                @Override
                public void listen(Progress data) {
                    finalProgressListener.listen(data);
                }
            };
            inputStreamReader = new InputStreamReader(inputStream, charset.getName());
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            int lineNum = 0;
            while (line != null) {
                if (range.getEnd() != null && lineNum >= range.getEnd()) {
                    break;
                }
                if (lineNum >= range.getStart()) {
                    textInputStreamReadListener.listen(line);
                    progress.add(line.getBytes().length);
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
     * 读取
     *
     * @param range 读取范围
     * @param textInputStreamReadListener textInputStreamReadListener
     * */
    public void read(Range range, TextInputStreamReadListener textInputStreamReadListener) {
        read(range, textInputStreamReadListener, null);
    }
    /**
     * 读取
     *
     * @param range 读取范围
     *
     * @return 读取到的数据
     * */
    public String read(Range range) {
        StringBuilder result = new StringBuilder();
        read(range, result::append);
        return result.toString();
    }

    /**
     * 读取
     *
     * @param textInputStreamReadListener textInputStreamReadListener
     * */
    public void read(TextInputStreamReadListener textInputStreamReadListener) {
        read(null, textInputStreamReadListener, null);
    }


    /**
     * 读取
     *
     * @return 文件数据
     * */
    public String read() {
        return read(new Range(0, null));
    }

}