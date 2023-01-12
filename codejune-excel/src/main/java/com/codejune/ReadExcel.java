package com.codejune;

import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.FileUtil;
import com.codejune.common.util.StringUtil;
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

    public ReadExcel(String path) {
        if (StringUtil.isEmpty(path) || !FileUtil.exist(new File(path))) {
            throw new InfoException("excel文件不存在");
        }
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            Workbook workbook;
            if (path.endsWith(".xls")) {
                workbook = WorkbookFactory.create(fileInputStream);
            } else if (path.endsWith(".xlsx")) {
                workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileInputStream);
            } else {
                throw new InfoException("文件错误");
            }
            this.workbook = workbook;
        } catch (Exception e) {
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
        closeWorkbook(this.workbook);
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

    private void closeWorkbook(Workbook workbook) {
        if (workbook == null) {
            return;
        }
        try {
            workbook.close();
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}