package com.codejune.excel;

import com.codejune.common.BaseException;
import com.codejune.common.util.StringUtil;
import org.apache.poi.ss.usermodel.Workbook;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Sheet
 *
 * @author ZJ
 * */
public final class Sheet implements Iterable<Row> {

    private final org.apache.poi.ss.usermodel.Sheet sheet;

    private final Workbook workbook;

    public Sheet(org.apache.poi.ss.usermodel.Sheet sheet, Workbook workbook) {
        this.sheet = sheet;
        this.workbook = workbook;
        this.sheet.setForceFormulaRecalculation(true);
    }

    /**
     * 获取名称
     *
     * @return 名称
     * */
    public String getName() {
        return this.sheet.getSheetName();
    }

    /**
     * 设置名称
     *
     * @param name 名称
     * */
    public void setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new BaseException("名称不能为空");
        }
        this.workbook.setSheetName(this.workbook.getSheetIndex(this.getName()), name);
    }

    /**
     * 获取index
     *
     * @return index
     * */
    public int getIndex() {
        return this.workbook.getSheetIndex(this.getName());
    }

    /**
     * 获取row
     *
     * @param rowIndex rowIndex
     *
     * @return Row
     * */
    public Row getRow(int rowIndex) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(rowIndex);
        if (row == null) {
            row = this.sheet.createRow(rowIndex);
        }
        return new Row(this, row);
    }

    /**
     * 获取所有行号
     *
     * @return 最后的行号
     * */
    public int getRowSize() {
        return this.sheet.getLastRowNum() + 1;
    }

    /**
     * 插入一行
     *
     * @param rowIndex 插入的位置
     *
     * @return Row
     * */
    public Row insertRow(int rowIndex) {
        this.sheet.shiftRows(rowIndex, this.sheet.getLastRowNum(), 1);
        return new Row(this, this.sheet.createRow(rowIndex));
    }

    /**
     * 删除row
     *
     * @param rowIndex rowIndex
     * */
    public void deleteRow(int rowIndex) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(rowIndex);
        if (row != null) {
            this.sheet.removeRow(row);
        }
    }

    /**
     * 删除cell
     *
     * @param cellIndex cellIndex
     * */
    public void deleteCell(int cellIndex) {
        for (Row row : this) {
            row.deleteCell(cellIndex);
        }
    }

    @Override
    public Iterator<Row> iterator() {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < getRowSize(); i++) {
            rows.add(getRow(i));
        }
        int[] i = new int[] {0};
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i[0] < rows.size();
            }

            @Override
            public Row next() {
                return rows.get(i[0]++);
            }
        };
    }

}