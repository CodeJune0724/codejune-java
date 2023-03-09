package com.codejune;

import com.codejune.common.util.*;
import com.codejune.common.util.DateUtil;
import com.monitorjbl.xlsx.StreamingReader;
import com.codejune.common.Closeable;
import com.codejune.common.exception.InfoException;
import com.codejune.excel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * excel
 *
 * @author ZJ
 * */
public final class Excel implements Closeable, Iterable<Sheet> {

    private final Workbook workbook;

    public Excel() {
        workbook = new SXSSFWorkbook(-1);
        this.addSheet("Sheet1");
    }

    public Excel(String path) {
        if (StringUtil.isEmpty(path) || !FileUtil.exist(new File(path))) {
            throw new InfoException("excel文件不存在");
        }
        Workbook tmpWorkbook = null;
        try (
                FileInputStream fileInputStream = new FileInputStream(path)
        ) {
            if (path.endsWith(".xls")) {
                tmpWorkbook = WorkbookFactory.create(fileInputStream);
            } else if (path.endsWith(".xlsx")) {
                tmpWorkbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(fileInputStream);
            } else {
                throw new InfoException("文件错误");
            }
            this.workbook = new SXSSFWorkbook(-1);
            Iterator<org.apache.poi.ss.usermodel.Sheet> sheetIterator = tmpWorkbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                org.apache.poi.ss.usermodel.Sheet tmpSheet = sheetIterator.next();
                org.apache.poi.ss.usermodel.Sheet sheet = this.workbook.createSheet(tmpSheet.getSheetName());
                int index = 0;
                for (Row tmpRow : tmpSheet) {
                    Row row = sheet.createRow(index);
                    short lastCellNum = tmpRow.getLastCellNum();
                    for (int i = 0; i < lastCellNum; i++) {
                        Cell cell = tmpRow.getCell(i);
                        String cellData = "";
                        if (cell != null) {
                            String c;
                            if (cell.getCellType() == CellType.NUMERIC) {
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    c = DateUtil.format(cell.getDateCellValue(), DateUtil.DEFAULT_DATE_FORMAT);
                                } else {
                                    c = ObjectUtil.toString(cell.getNumericCellValue());
                                }
                            } else {
                                c = cell.getStringCellValue();
                            }
                            if (c != null) {
                                cellData = c;
                            }
                        }
                        row.createCell(i).setCellValue(cellData);
                    }
                    index++;
                }
            }
        } catch (Exception e) {
            this.close();
            throw new InfoException(e.getMessage());
        } finally {
            closeWorkbook(tmpWorkbook);
        }
    }

    public Excel(File excelFile) {
        this(excelFile == null ? null : excelFile.getAbsolutePath());
    }

    /**
     * 添加sheet
     *
     * @param sheetName sheet名
     *
     * @return Sheet
     * */
    public Sheet addSheet(String sheetName) {
        if (this.workbook.getSheet(sheetName) != null) {
            throw new InfoException(sheetName + "已存在");
        }
        return new Sheet(this.workbook.createSheet(sheetName), this.workbook);
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
            return null;
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
     * 获取所有sheet
     *
     * @return List
     * */
    public List<Sheet> getSheets() {
        List<Sheet> result = new ArrayList<>();
        Iterator<org.apache.poi.ss.usermodel.Sheet> sheetIterator = this.workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            result.add(new Sheet(sheetIterator.next(), this.workbook));
        }
        return result;
    }

    /**
     * 保存
     *
     * @param file file
     * */
    public void save(File file) {
        if (file == null) {
            return;
        }
        if (!file.getName().endsWith(".xlsx")) {
            throw new InfoException("文件错误");
        }
        OutputStream outputStream = null;
        try {
            outputStream = Files.newOutputStream(file.toPath());
            this.workbook.write(outputStream);
        } catch (Exception e) {
            IOUtil.close(outputStream);
        }
    }

    @Override
    public void close() {
        closeWorkbook(this.workbook);
    }

    @Override
    public Iterator<Sheet> iterator() {
        List<Sheet> sheets = getSheets();
        final int[] i = new int[]{0};
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i[0] < sheets.size();
            }

            @Override
            public Sheet next() {
                return sheets.get(i[0]++);
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