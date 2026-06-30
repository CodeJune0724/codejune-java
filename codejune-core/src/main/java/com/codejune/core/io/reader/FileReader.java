package com.codejune.core.io.reader;

import com.codejune.core.Range;
import com.codejune.core.BaseException;
import com.codejune.core.io.Reader;
import com.codejune.core.util.IOUtil;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * 文件读取器
 *
 * @author ZJ
 * */
public final class FileReader extends Reader {

    private final java.io.File file;

    public FileReader(java.io.File file) {
        super(IOUtil.getInputStream(file));
        this.file = file;
    }

    /**
     * 范围读取
     *
     * @param consumer consumer
     * @param range 读取范围
     * */
    public void read(Consumer<ByteBuffer> consumer, Range range) {
        if (range == null || range.getStart() == null || range.getEnd() == null || range.getEnd() <= range.getStart()) {
            throw new BaseException("range error");
        }
        if (consumer == null) {
            consumer = _ -> {};
        }
        this.setReadSize((int) (range.getEnd() - range.getStart()));
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "r")) {
            randomAccessFile.seek(range.getStart());
            byte[] bytes = new byte[this.getReadSize()];
            int size = randomAccessFile.read(bytes);
            consumer.accept(ByteBuffer.wrap(bytes, 0, size));
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}