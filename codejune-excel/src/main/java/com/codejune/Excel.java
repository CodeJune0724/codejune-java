package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.Closeable;
import com.codejune.core.util.*;
import com.codejune.excel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * excel
 *
 * @author ZJ
 * */
public final class Excel implements Closeable, Iterable<Sheet> {

    private final Workbook workbook;

    private XSSFWorkbook xssfWorkbook = null;

    public Excel(boolean write) {
        try {
            if (write) {
                this.workbook = new SXSSFWorkbook(100);
            } else {
                this.workbook = new SXSSFWorkbook(-1);
            }
            this.workbook.createSheet("Sheet1");
        } catch (Exception e) {
            this.close();
            throw new BaseException(e);
        }
    }

    public Excel() {
        this(false);
    }

    public Excel(InputStream inputStream, boolean write) {
        if (inputStream == null) {
            throw new BaseException("inputStream is null");
        }
        try {
            if (write) {
                this.xssfWorkbook = new XSSFWorkbook(inputStream);
                this.workbook = new SXSSFWorkbook(this.xssfWorkbook, 100);
            } else {
                this.workbook = WorkbookFactory.create(inputStream);
            }
        } catch (Exception exception) {
            throw new BaseException(exception);
        }
    }

    public Excel(InputStream inputStream) {
        this(inputStream, false);
    }

    public Excel(String path, boolean write) {
        if (StringUtil.isEmpty(path) || !FileUtil.exist(new java.io.File(path))) {
            throw new BaseException("excel文件不存在");
        }
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            if (write) {
                this.xssfWorkbook = new XSSFWorkbook(path);
                this.workbook = new SXSSFWorkbook(this.xssfWorkbook, 100);
            } else {
                this.workbook = WorkbookFactory.create(fileInputStream);
            }
        } catch (Exception e) {
            this.close();
            throw new BaseException(e);
        }
    }

    public Excel(String path) {
        this(path, false);
    }

    public Excel(java.io.File excelFile, boolean write) {
        this(excelFile == null ? null : excelFile.getAbsolutePath(), write);
    }

    public Excel(java.io.File excelFile) {
        this(excelFile == null ? null : excelFile.getAbsolutePath(), false);
    }

    /**
     * 删除sheet
     *
     * @param sheetName sheet名
     * */
    public void deleteSheet(String sheetName) {
        if (this.workbook.getSheet(sheetName) == null) {
            return;
        }
        this.workbook.removeSheetAt(this.workbook.getSheetIndex(sheetName));
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
            sheet = this.workbook.createSheet(sheetName);
        }
        return new Sheet(sheet);
    }

    /**
     * 获取sheet
     *
     * @param sheetIndex sheet序号
     *
     * @return Sheet
     * */
    public Sheet getSheet(int sheetIndex) {
        org.apache.poi.ss.usermodel.Sheet sheetAt = this.workbook.getSheetAt(sheetIndex);
        if (sheetAt == null) {
            return null;
        }
        return new Sheet(sheetAt);
    }

    /**
     * 获取sheet数量
     *
     * @return sheet数量
     * */
    public int getSheetNumber() {
        return this.workbook.getNumberOfSheets();
    }

    /**
     * 保存
     *
     * @param outputStream outputStream
     * */
    public void save(OutputStream outputStream) {
        if (outputStream == null) {
            return;
        }
        try {
            this.workbook.write(outputStream);
            outputStream.flush();
            if (this.workbook instanceof SXSSFWorkbook sxssfWorkbook) {
                sxssfWorkbook.dispose();
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 保存
     *
     * @param file file
     *
     * @return file
     * */
    public File save(File file) {
        try (OutputStream outputStream = IOUtil.getOutputStream(file)) {
            this.save(outputStream);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return file;
    }

    @Override
    public void close() {
        if (this.workbook != null) {
            try {
                workbook.close();
            } catch (Exception ignored) {}
        }
        if (this.xssfWorkbook != null) {
            try {
                xssfWorkbook.close();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public Iterator<Sheet> iterator() {
        Iterator<org.apache.poi.ss.usermodel.Sheet> sheetIterator = this.workbook.sheetIterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return sheetIterator.hasNext();
            }

            @Override
            public Sheet next() {
                return new Sheet(sheetIterator.next());
            }
        };
    }

}