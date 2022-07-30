package com.codejune.excel;

import com.codejune.common.util.ObjectUtil;

/**
 * Cell
 *
 * @author ZJ
 * */
public final class Cell {

    private final org.apache.poi.ss.usermodel.Cell cell;

    Cell(org.apache.poi.ss.usermodel.Cell cell) {
        this.cell = cell;
    }

    /**
     * 获取值
     *
     * @return 值
     * */
    public String getValue() {
        return this.cell.getStringCellValue();
    }

    /**
     * 设置值
     *
     * @param value 值
     * */
    public void setValue(Object value) {
        String setValue = "";
        if (value != null) {
            setValue = ObjectUtil.toString(value);
        }
        this.cell.setCellValue(setValue);
    }

}