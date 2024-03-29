package com.codejune.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Row
 *
 * @author ZJ
 * */
public final class Row implements Iterable<Cell> {

    private final Sheet sheet;

    private final org.apache.poi.ss.usermodel.Row row;

    Row(Sheet sheet, org.apache.poi.ss.usermodel.Row row) {
        this.sheet = sheet;
        this.row = row;
    }

    /**
     * 获取index
     *
     * @return index
     * */
    public int getIndex() {
        return this.row.getRowNum();
    }

    /**
     * 获取样式
     *
     * @return 样式
     * */
    public CellStyle getStyle() {
        return this.row.getRowStyle();
    }

    /**
     * 设置样式
     *
     * @param cellStyle 样式
     * */
    public void setStyle(CellStyle cellStyle) {
        this.row.setRowStyle(cellStyle);
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
            cell = this.row.createCell(cellIndex);
        }
        return new Cell(this, cell);
    }

    /**
     * 删除cell
     *
     * @param cellIndex celIndex
     * */
    public void deleteCell(int cellIndex) {
        org.apache.poi.ss.usermodel.Cell cell = this.row.getCell(cellIndex);
        int cellSize = this.getCellSize();
        if (cell != null) {
            this.row.removeCell(cell);
        }
        for (int i = cellIndex + 1; i < cellSize; i++) {
            this.getCell(i).copy(this.getIndex(), i - 1);
        }
        org.apache.poi.ss.usermodel.Cell finallyCell = this.row.getCell(cellSize - 1);
        if (finallyCell != null) {
            this.row.removeCell(finallyCell);
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

    /**
     * 获取sheet
     *
     * @return Sheet
     * */
    public Sheet getSheet() {
        return this.sheet;
    }

    @Override
    public Iterator<Cell> iterator() {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < getCellSize(); i++) {
            cells.add(getCell(i));
        }
        int[] i = new int[] {0};
        return new Iterator<>() {
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