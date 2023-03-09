package com.codejune.excel;

import com.codejune.common.exception.InfoException;
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
            throw new InfoException("名称不能为空");
        }
        this.workbook.setSheetName(this.workbook.getSheetIndex(this.getName()), name);
    }

    /**
     * 获取row
     *
     * @param rowNum 序号
     *
     * @return Row
     * */
    public Row getRow(int rowNum) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(rowNum);
        if (row == null) {
            row = this.sheet.createRow(rowNum);
        }
        return new Row(row);
    }

    /**
     * 删除row
     *
     * @param rowNum 序号
     * */
    public void deleteRow(int rowNum) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(rowNum);
        if (row != null) {
            this.sheet.removeRow(row);
        }
    }

    /**
     * 获取所有行号
     *
     * @return 最后的行号
     * */
    public int getRowSize() {
        return this.sheet.getLastRowNum() + 1;
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