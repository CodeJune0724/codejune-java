package com.codejune.excel;

import com.codejune.core.BaseException;
import com.codejune.core.Range;
import com.codejune.core.io.reader.InputStreamReader;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sheet
 *
 * @author ZJ
 * */
public final class Sheet implements Iterable<Row> {

    private final org.apache.poi.ss.usermodel.Sheet sheet;

    public Sheet(org.apache.poi.ss.usermodel.Sheet sheet) {
        this.sheet = sheet;
        this.sheet.setForceFormulaRecalculation(true);
    }

    /**
     * 获取名称
     *
     * @return 名称
     * */
    public String getName() {
        return this.sheet.getSheetName();
    }

    /**
     * 设置名称
     *
     * @param name 名称
     * */
    public void setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new BaseException("名称不能为空");
        }
        this.sheet.getWorkbook().setSheetName(this.sheet.getWorkbook().getSheetIndex(this.getName()), name);
    }

    /**
     * 获取index
     *
     * @return index
     * */
    public int getIndex() {
        return this.sheet.getWorkbook().getSheetIndex(this.getName());
    }

    /**
     * 获取row
     *
     * @param rowIndex rowIndex
     *
     * @return Row
     * */
    public Row getRow(int rowIndex) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(rowIndex);
        if (row == null) {
            row = this.sheet.createRow(rowIndex);
        }
        return new Row(this, row);
    }

    /**
     * 获取下一行
     *
     * @return 下一行
     * */
    public Row getNextRow() {
        return this.getRow(this.getRowSize());
    }

    /**
     * 获取所有行号
     *
     * @return 最后的行号
     * */
    public int getRowSize() {
        return this.sheet.getLastRowNum() + 1;
    }

    /**
     * 插入一行
     *
     * @param rowIndex 插入的位置
     *
     * @return Row
     * */
    public Row insertRow(int rowIndex) {
        this.sheet.shiftRows(rowIndex, this.sheet.getLastRowNum(), 1);
        return new Row(this, this.sheet.createRow(rowIndex));
    }

    /**
     * 删除row
     *
     * @param rowIndex rowIndex
     * */
    public void deleteRow(int rowIndex) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(rowIndex);
        if (row != null) {
            this.sheet.removeRow(row);
        }
    }

    /**
     * 删除cell
     *
     * @param cellIndex cellIndex
     * */
    public void deleteCell(int cellIndex) {
        for (Row row : this) {
            row.deleteCell(cellIndex);
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
        if (this.sheet instanceof XSSFSheet xssfSheet) {
            for (POIXMLDocumentPart poixmlDocumentPart : xssfSheet.getRelations()) {
                if (poixmlDocumentPart instanceof XSSFDrawing xssfDrawing) {
                    for (XSSFShape xssfShape : xssfDrawing.getShapes()) {
                        if (xssfShape instanceof XSSFPicture xssfPicture) {
                            CTMarker ctMarker = xssfPicture.getPreferredSize().getFrom();
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

    /**
     * 合并单元格
     *
     * @param startRowIndex startRowIndex
     * @param endRowIndex endRowIndex
     * @param startCellIndex startCellIndex
     * @param endCellIndex endCellIndex
     * */
    public void merge(int startRowIndex, int endRowIndex, int startCellIndex, int endCellIndex) {
        if (startRowIndex == endRowIndex && startCellIndex == endCellIndex) {
            return;
        }
        this.sheet.addMergedRegion(new CellRangeAddress(startRowIndex, endRowIndex, startCellIndex, endCellIndex));
    }

    /**
     * copy
     *
     * @return Sheet
     * */
    public Sheet copy() {
        Sheet result = new Sheet(this.sheet.getWorkbook().createSheet());
        for (int i = 0; i < this.sheet.getNumMergedRegions(); i++) {
            result.sheet.addMergedRegion(this.sheet.getMergedRegion(i));
        }
        for (int i = 0; i <= this.getRowSize(); i++) {
            result.sheet.setColumnWidth(i, this.sheet.getColumnWidth(i));
        }
        for (Row row : this) {
            row.copy(result.getRow(row.getIndex()));
        }
        return result;
    }

    /**
     * 添加图片
     *
     * @param inputStream inputStream
     * @param rowRange rowRange
     * @param cellRange cellRange
     * */
    public void addImage(InputStream inputStream, Range rowRange, Range cellRange) {
        Drawing<?> drawingPatriarch = this.sheet.createDrawingPatriarch();
        int pictureIndex = this.sheet.getWorkbook().addPicture(new InputStreamReader(inputStream).getByte(), Workbook.PICTURE_TYPE_PNG);
        CreationHelper creationHelper = this.sheet.getWorkbook().getCreationHelper();
        ClientAnchor clientAnchor = creationHelper.createClientAnchor();
        clientAnchor.setRow1(ObjectUtil.parse(rowRange.getStart(), int.class));
        clientAnchor.setRow2(ObjectUtil.parse(rowRange.getEnd(), int.class));
        clientAnchor.setCol1(ObjectUtil.parse(cellRange.getStart(), int.class));
        clientAnchor.setCol2(ObjectUtil.parse(cellRange.getEnd(), int.class));
        drawingPatriarch.createPicture(clientAnchor, pictureIndex);
    }

    @Override
    public Iterator<Row> iterator() {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < getRowSize(); i++) {
            rows.add(getRow(i));
        }
        int[] i = new int[] {0};
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i[0] < rows.size();
            }

            @Override
            public Row next() {
                return rows.get(i[0]++);
            }
        };
    }

}