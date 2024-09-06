package com.codejune.excel;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.function.Consumer;

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
     * 获取值
     *
     * @param tClass tClass
     * @param <T> T
     *
     * @return 值
     * */
    public <T> T getValue(Class<T> tClass) {
        return ObjectUtil.parse(this.getValue(), tClass);
    }

    /**
     * 设置值
     *
     * @param value 值
     * */
    public void setValue(Object value) {
        switch (value) {
            case null -> this.cell.setCellValue("");
            case Date date -> this.cell.setCellValue(date);
            case Double d -> this.cell.setCellValue(d);
            case Boolean b -> this.cell.setCellValue(b);
            default -> this.cell.setCellValue(ObjectUtil.toString(value));
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
     * @param cell cell
     * */
    public void copy(Cell cell) {
        if (cell == null) {
            return;
        }
        CellStyle cellStyle = this.cell.getRow().getSheet().getWorkbook().createCellStyle();
        cellStyle.cloneStyleFrom(this.cell.getCellStyle());
        cell.cell.setCellStyle(cellStyle);
        cell.cell.setCellComment(this.cell.getCellComment());
        CellType cellType = this.cell.getCellType();
        if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(this.cell)) {
                cell.cell.setCellValue(this.cell.getDateCellValue());
            } else {
                cell.cell.setCellValue(this.cell.getNumericCellValue());
            }
        } else if (cellType == CellType.STRING) {
            cell.cell.setCellValue(this.cell.getRichStringCellValue());
        } else if (cellType == CellType.BOOLEAN) {
            cell.cell.setCellValue(this.cell.getBooleanCellValue());
        } else if (cellType == CellType.ERROR) {
            cell.cell.setCellErrorValue(this.cell.getErrorCellValue());
        } else if (cellType == CellType.FORMULA) {
            cell.cell.setCellFormula(this.cell.getCellFormula());
        }
    }

    /**
     * 获取图片
     *
     * @param consumer consumer
     * */
    public void getImage(Consumer<Image> consumer) {
        if (consumer == null) {
            return;
        }
        Sheet sheet = this.cell.getSheet();
        if (sheet instanceof XSSFSheet xssfSheet) {
            for (POIXMLDocumentPart poixmlDocumentPart : xssfSheet.getRelations()) {
                if (poixmlDocumentPart instanceof XSSFDrawing xssfDrawing) {
                    for (XSSFShape xssfShape : xssfDrawing.getShapes()) {
                        if (xssfShape instanceof XSSFPicture xssfPicture) {
                            CTMarker ctMarker = xssfPicture.getPreferredSize().getFrom();
                            if (this.getRow().getIndex() == ctMarker.getRow() && this.getIndex() == ctMarker.getCol()) {
                                XSSFPictureData pictureData = xssfPicture.getPictureData();
                                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pictureData.getData())) {
                                    consumer.accept(new Image(ctMarker.getRow(), ctMarker.getCol(), pictureData.suggestFileExtension(), byteArrayInputStream));
                                } catch (Exception e) {
                                    throw new BaseException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取样式
     *
     * @return 样式
     * */
    public CellStyle getStyle() {
        return this.cell.getCellStyle();
    }

}