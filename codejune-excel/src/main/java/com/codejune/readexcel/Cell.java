package com.codejune.readexcel;

/**
 * Cell
 *
 * @author ZJ
 * */
public final class Cell {

    private final org.apache.poi.ss.usermodel.Cell cell;

    public Cell(org.apache.poi.ss.usermodel.Cell cell) {
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

}