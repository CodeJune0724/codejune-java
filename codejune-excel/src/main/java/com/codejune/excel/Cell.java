package com.codejune.excel;

import com.codejune.common.util.ObjectUtil;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import java.util.Date;

/**
 * Cell
 *
 * @author ZJ
 * */
public final class Cell {

    private final Row row;

    private final org.apache.poi.ss.usermodel.Cell cell;

    Cell(Row row, org.apache.poi.ss.usermodel.Cell cell) {
        this.row = row;
        this.cell = cell;
    }

    @Override
    public String toString() {
        return this.cell.toString();
    }

    /**
     * 获取值
     *
     * @return 值
     * */
    public Object getValue() {
        CellType cellType = this.cell.getCellType();
        if (cellType == CellType.STRING) {
            return this.cell.getStringCellValue();
        }
        if (cellType == CellType.NUMERIC) {
            return this.cell.getNumericCellValue();
        }
        if (cellType == CellType.BOOLEAN) {
            return this.cell.getBooleanCellValue();
        }
        if (cellType == CellType.ERROR) {
            return this.cell.getErrorCellValue();
        }
        if (cellType == CellType.FORMULA) {
            String result;
            try {
                result = String.valueOf(cell.getNumericCellValue());
            } catch (Exception e) {
                return this.toString();
            }
            if ("NaN".equals(result)) {
                result = cell.getRichStringCellValue().toString();
            }
            return result;
        }
        if (DateUtil.isCellDateFormatted(this.cell)) {
            return this.cell.getDateCellValue();
        }
        return this.cell.getStringCellValue();
    }

    /**
     * 设置值
     *
     * @param value 值
     * */
    public void setValue(Object value) {
        if (value == null) {
            this.cell.setCellValue("");
        } else if (value instanceof Date date) {
            this.cell.setCellValue(date);
        } else if (value instanceof Double d) {
            this.cell.setCellValue(d);
        } else if (value instanceof Boolean b) {
            this.cell.setCellValue(b);
        } else {
            this.cell.setCellValue(ObjectUtil.toString(value));
        }
    }

    /**
     * 获取index
     *
     * @return index
     * */
    public int getIndex() {
        return this.cell.getColumnIndex();
    }

    /**
     * 获取样式
     *
     * @return 样式
     * */
    public CellStyle getStyle() {
        return this.cell.getCellStyle();
    }

    /**
     * 设置样式
     *
     * @param cellStyle 样式
     * */
    public void setStyle(CellStyle cellStyle) {
        this.cell.setCellStyle(cellStyle);
    }

    /**
     * 获取row
     *
     * @return row
     * */
    public Row getRow() {
        return this.row;
    }

    /**
     * copy
     *
     * @param rowIndex rowIndex
     * @param cellIndex cellIndex
     * */
    public void copy(int rowIndex, int cellIndex) {
        Cell newCell = this.row.getSheet().getRow(rowIndex).getCell(cellIndex);
        newCell.cell.setCellComment(this.cell.getCellComment());
        newCell.setStyle(this.getStyle());
        newCell.setValue(this.getValue());
    }

    /**
     * 获取图片
     *
     * @return Image
     * */
    public Image getImage() {
        Sheet sheet = this.cell.getSheet();
        if (sheet instanceof XSSFSheet xssfSheet) {
            for (POIXMLDocumentPart poixmlDocumentPart : xssfSheet.getRelations()) {
                if (poixmlDocumentPart instanceof XSSFDrawing xssfDrawing) {
                    for (XSSFShape xssfShape : xssfDrawing.getShapes()) {
                        if (xssfShape instanceof XSSFPicture xssfPicture) {
                            CTMarker ctMarker = xssfPicture.getPreferredSize().getFrom();
                            if (this.getRow().getIndex() == ctMarker.getRow() && this.getIndex() == ctMarker.getCol()) {
                                XSSFPictureData pictureData = xssfPicture.getPictureData();
                                return new Image(ctMarker.getRow(), ctMarker.getCol(), pictureData.suggestFileExtension(), pictureData.getData());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static class Image {

        private final int rowIndex;

        private final int cellIndex;

        private final String suffix;

        private final byte[] data;

        public Image(int rowIndex, int cellIndex, String suffix, byte[] data) {
            this.rowIndex = rowIndex;
            this.cellIndex = cellIndex;
            this.suffix = suffix;
            this.data = data;
        }

        public String getSuffix() {
            return suffix;
        }

        public byte[] getData() {
            return data;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public int getCellIndex() {
            return cellIndex;
        }

    }

}