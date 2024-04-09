package com.codejune.excel;

import java.io.InputStream;

/**
 * 图片
 *
 * @author ZJ
 * */
public final class Image {

    private final int rowIndex;

    private final int cellIndex;

    private final String suffix;

    private final InputStream data;

    public Image(int rowIndex, int cellIndex, String suffix, InputStream data) {
        this.rowIndex = rowIndex;
        this.cellIndex = cellIndex;
        this.suffix = suffix;
        this.data = data;
    }

    public String getSuffix() {
        return suffix;
    }

    public InputStream getData() {
        return data;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getCellIndex() {
        return cellIndex;
    }

}