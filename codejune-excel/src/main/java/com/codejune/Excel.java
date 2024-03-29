package com.codejune;

import com.codejune.common.BaseException;
import com.codejune.common.Closeable;
import com.codejune.common.os.File;
import com.codejune.common.util.*;
import com.codejune.excel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
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
        return new Sheet(sheet, this.workbook);
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
        return new Sheet(sheetAt, this.workbook);
    }

    /**
     * 保存
     *
     * @param result file
     *
     * @return file
     * */
    public java.io.File save(java.io.File result) {
        if (result == null) {
            return null;
        }
        if (!result.getName().endsWith(".xlsx")) {
            throw new BaseException("文件错误");
        }
        new File(result.getAbsolutePath());
        try (OutputStream outputStream = IOUtil.getOutputStream(result)) {
            this.workbook.write(outputStream);
            outputStream.flush();
            if (this.workbook instanceof SXSSFWorkbook sxssfWorkbook) {
                sxssfWorkbook.dispose();
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return result;
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
                return new Sheet(sheetIterator.next(), workbook);
            }
        };
    }

}