package com.codejune.excelreader;

/**
 * Cell
 *
 * @author ZJ
 * */
public final class Cell {

    private final int index;

    private final String value;

    public Cell(int index, String value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getValue() {
        return this.value;
    }

}