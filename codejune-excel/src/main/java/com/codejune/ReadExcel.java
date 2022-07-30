package com.codejune;

import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.IOUtil;
import com.codejune.readexcel.Sheet;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

/**
 * 读excel
 *
 * @author ZJ
 * */
public final class ReadExcel implements Closeable, Iterable<Sheet> {

    private final Workbook workbook;

    private final FileInputStream fileInputStream;

    public ReadExcel(String path) {
        FileInputStream fileInputStream;
        Workbook workbook;
        try {
            fileInputStream = new FileInputStream(path);
            if (path.endsWith(".xls")) {
                workbook = WorkbookFactory.create(fileInputStream);
            } else if (path.endsWith(".xlsx")) {
                workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileInputStream);
            } else {
                throw new InfoException("文件错误");
            }
            this.fileInputStream = fileInputStream;
            this.workbook = workbook;
        } catch (Exception e) {
            e.printStackTrace();
            this.close();
            throw new InfoException(e.getMessage());
        }
    }

    public ReadExcel(File file) {
        this(file == null ? null : file.getAbsolutePath());
    }

    /**
     * 获取sheet
     *
     * @param sheetName sheet名
     *
     * @return Sheet
     * */
    public Sheet getSheet(String sheetName) {
        org.apache.poi.ss.usermodel.Sheet sheet = this.workbook.getSheet(sheetName);
        if (sheet == null) {
            return null;
        }
        return new Sheet(sheet);
    }

    @Override
    public void close() {
        try {
            workbook.close();
            IOUtil.close(fileInputStream);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public Iterator<Sheet> iterator() {
        Iterator<org.apache.poi.ss.usermodel.Sheet> iterator = workbook.iterator();
        return new Iterator<Sheet>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Sheet next() {
                return new Sheet(iterator.next());
            }
        };
    }

}