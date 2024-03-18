package com.codejune.excelreader;

/**
 * Cell
 *
 * @author ZJ
 * */
public final class Cell {

    private final int index;

    private final String value;

    private final Row row;

    public Cell(int index, String value, Row row) {
        this.index = index;
        this.value = value;
        this.row = row;
    }

    public int getIndex() {
        return index;
    }

    public String getValue() {
        return this.value;
    }

    public Row getRow() {
        return row;
    }

}