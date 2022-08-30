package com.codejune.excel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Row
 *
 * @author ZJ
 * */
public final class Row implements Iterable<Cell> {

    private final org.apache.poi.ss.usermodel.Row row;

    Row(org.apache.poi.ss.usermodel.Row row) {
        this.row = row;
    }

    /**
     * 获取cell
     *
     * @param cellNum cell序号
     *
     * @return Cell
     * */
    public Cell getCell(int cellNum) {
        org.apache.poi.ss.usermodel.Cell cell = this.row.getCell(cellNum);
        if (cell == null) {
            cell = this.row.createCell(cellNum);
        }
        return new Cell(cell);
    }

    /**
     * 删除cell
     *
     * @param cellNum cell序号
     * */
    public void deleteCell(int cellNum) {
        org.apache.poi.ss.usermodel.Cell cell = this.row.getCell(cellNum);
        if (cell != null) {
            this.row.removeCell(cell);
        }
    }

    /**
     * 获取列大小
     *
     * @return 最后的列号
     * */
    public int getCellSize() {
        return this.row.getLastCellNum();
    }

    @Override
    public Iterator<Cell> iterator() {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < getCellSize(); i++) {
            cells.add(getCell(i));
        }
        int[] i = new int[] {0};
        return new Iterator<Cell>() {
            @Override
            public boolean hasNext() {
                return i[0] < cells.size();
            }

            @Override
            public Cell next() {
                return cells.get(i[0]++);
            }
        };
    }

}