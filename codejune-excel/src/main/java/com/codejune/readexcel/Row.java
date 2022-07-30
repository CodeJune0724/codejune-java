package com.codejune.readexcel;

import java.util.Iterator;

/**
 * Row
 *
 * @author ZJ
 * */
public final class Row implements Iterable<Cell> {

    private final org.apache.poi.ss.usermodel.Row row;

    private final short cellNum;

    public Row(org.apache.poi.ss.usermodel.Row row) {
        this.row = row;
        this.cellNum = row.getLastCellNum();
    }

    public short getCellNum() {
        return cellNum;
    }

    /**
     * 获取cell
     *
     * @param cellIndex cellIndex
     *
     * @return Cell
     * */
    public Cell getCell(int cellIndex) {
        org.apache.poi.ss.usermodel.Cell cell = this.row.getCell(cellIndex);
        if (cell == null) {
            return null;
        }
        return new Cell(cell);
    }

    @Override
    public Iterator<Cell> iterator() {
        Iterator<org.apache.poi.ss.usermodel.Cell> iterator = row.iterator();
        return new Iterator<Cell>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Cell next() {
                return new Cell(iterator.next());
            }
        };
    }

}