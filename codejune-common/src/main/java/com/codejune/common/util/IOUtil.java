package com.codejune.common.util;

import com.codejune.common.Progress;
import com.codejune.common.exception.InfoException;
import com.codejune.common.listener.InputStreamListener;
import com.codejune.common.listener.ProgressListener;
import com.codejune.common.model.Charset;
import java.io.*;

/**
 * IOUtil
 *
 * @author ZJ
 * */
public final class IOUtil {

    /**
     * 关闭inputStream
     *
     * @param inputStream inputStream
     * */
    public static void close(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 关闭reader
     *
     * @param reader reader
     * */
    public static void close(Reader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 关闭outputStream
     *
     * @param outputStream outputStream
     * */
    public static void close(OutputStream outputStream) {
        if (outputStream == null) {
            return;
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 关闭outputStream
     *
     * @param writer writer
     * */
    public static void close(Writer writer) {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 将流转成字符串
     *
     * @param inputStream inputStream
     * @param charset 字符编码
     * @param inputStreamListener 监听器
     *
     * @return 字符串
     * */
    public static String toString(InputStream inputStream, Charset charset, InputStreamListener inputStreamListener) {
        if (inputStream == null) {
            return null;
        }
        if (inputStreamListener == null) {
            inputStreamListener = data -> {};
        }
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, charset.getName());
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            String result = line == null ? null : "";
            while (line != null) {
                inputStreamListener.listen(line);
                result = StringUtil.append(result, line, "\n");
                line = bufferedReader.readLine();
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            close(inputStreamReader);
            close(bufferedReader);
        }
    }

    public static String toString(InputStream inputStream, Charset charset) {
        return toString(inputStream, charset, null);
    }

    public static String toString(InputStream inputStream) {
        return toString(inputStream, Charset.UTF_8);
    }

    /**
     * 写入数据
     *
     * @param outputStream 输出流
     * @param inputStream 输入流
     * @param progressListener 进度监听
     * */
    public static void write(OutputStream outputStream, InputStream inputStream, ProgressListener progressListener) {
        if (outputStream == null || inputStream == null) {
            return;
        }
        if (progressListener == null) {
            progressListener = data -> {};
        }
        Progress progress;
        try {
            ProgressListener finalProgressListener = progressListener;
            progress = new Progress(inputStream.available()) {
                @Override
                public void listen(Progress data) {
                    finalProgressListener.listen(data);
                }
            };
            byte[] bytes = new byte[1024];
            int read = inputStream.read(bytes);
            while (read != -1) {
                progress.addProgress(read);
                outputStream.write(bytes, 0, read);
                read = inputStream.read(bytes);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(outputStream);
        }
    }

    public static void write(OutputStream outputStream, InputStream inputStream) {
        write(outputStream, inputStream, null);
    }

}