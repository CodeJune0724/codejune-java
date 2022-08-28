package com.codejune.common.io.reader;

import com.codejune.common.Charset;
import com.codejune.common.Progress;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.AbstractReader;
import com.codejune.common.listener.ProgressListener;
import com.codejune.common.listener.TextReadListener;
import com.codejune.common.util.IOUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件读取器
 *
 * @author ZJ
 * */
public final class TextReader extends AbstractReader {

    private Charset charset = Charset.UTF_8;

    public TextReader(InputStream inputStream) {
        super(inputStream);
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * 读文件
     *
     * @param textReadListener textReadListener
     * @param progressListener progressListener
     * */
    public void read(TextReadListener textReadListener, ProgressListener progressListener) {
        if (textReadListener == null) {
            textReadListener = data -> {};
        }
        if (progressListener == null) {
            progressListener = data -> {};
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        ProgressListener finalProgressListener = progressListener;
        try {
            Progress progress = new Progress(this.inputStream.available()) {
                @Override
                public void listen(Progress data) {
                    finalProgressListener.listen(data);
                }
            };
            inputStreamReader = new InputStreamReader(inputStream, charset.getName());
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                textReadListener.listen(line);
                progress.add(line.getBytes().length);
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStreamReader);
            IOUtil.close(bufferedReader);
        }
    }

    /**
     * 读文件
     *
     * @param textReadListener textReadListener
     * */
    public void read(TextReadListener textReadListener) {
        read(textReadListener, null);
    }

    /**
     * 读取全部内容
     *
     * @return 文件数据
     * */
    public String read() {
        StringBuilder result = new StringBuilder();
        read(result::append);
        return result.toString();
    }

}