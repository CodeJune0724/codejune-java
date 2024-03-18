package com.codejune.excelreader;

import com.codejune.common.Listener;
import com.codejune.common.exception.InfoException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.RegexUtil;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Sheet
 *
 * @author ZJ
 * */
public final class Sheet {

    private final int index;

    private final XSSFReader xssfReader;

    private final SharedStrings sharedStrings;

    private final StylesTable stylesTable;

    public Sheet(int index, XSSFReader xssfReader, SharedStrings sharedStrings, StylesTable stylesTable) {
        this.index = index;
        this.xssfReader = xssfReader;
        this.sharedStrings = sharedStrings;
        this.stylesTable = stylesTable;
    }

    public int getIndex() {
        return index;
    }

    /**
     * 获取sheetName
     *
     * @return sheetName
     * */
    public String getSheetName() {
        try (InputStream workbookData = this.xssfReader.getWorkbookData()) {
            String sheetList = RegexUtil.find("<sheets>(.*?)</sheets>", new TextInputStreamReader(workbookData).getData(), 0);
            return RegexUtil.find(index == 0 ? "<sheet name=\"(.*?)\" sheetId=\"" + (index + 1) + "\" r:id=\"rId"  + (index + 1) +"\"/>" : "/><sheet name=\"(.*?)\" sheetId=\"" + (index + 1) + "\" r:id=\"rId" + (index + 1) + "\"/>", sheetList, 1);
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    /**
     * 读
     *
     * @param rowStart rowStart
     * @param cellListener cellListener
     * @param rowEnd rowEnd
     * */
    public void read(Listener<Row> rowStart, Listener<Cell> cellListener, Listener<Row> rowEnd) {
        if (rowStart == null) {
            rowStart = data -> {};
        }
        if (cellListener == null) {
            cellListener = data -> {};
        }
        if (rowEnd == null) {
            rowEnd = data -> {};
        }
        Listener<Row> finalRowStart = rowStart;
        Listener<Row> finalRowEnd = rowEnd;
        Listener<Cell> finalCellListener = cellListener;
        Map<Integer, String> dataMap = new HashMap<>();
        final int[] endIndex = {0};
        try (InputStream inputStream = this.xssfReader.getSheet("rId" + (this.index + 1))) {
            XMLReader xmlReader = SAXParserFactory.newNSInstance().newSAXParser().getXMLReader();
            xmlReader.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    if ("row".equals(localName)) {
                        Integer lastCell = ObjectUtil.transform(attributes.getValue("spans").replace("1:", ""), Integer.class);
                        if (lastCell > endIndex[0]) {
                            endIndex[0] = lastCell;
                        }
                    }
                }
            });
            xmlReader.parse(new InputSource(inputStream));
        } catch (Exception e) {
            throw new InfoException(e);
        }
        try (InputStream inputStream = this.xssfReader.getSheet("rId" + (this.index + 1))) {
            XMLReader xmlReader = SAXParserFactory.newNSInstance().newSAXParser().getXMLReader();
            xmlReader.setContentHandler(new XSSFSheetXMLHandler(this.stylesTable, this.sharedStrings, new XSSFSheetXMLHandler.SheetContentsHandler() {
                @Override
                public void startRow(int rowIndex) {
                    dataMap.clear();
                    finalRowStart.then(new Row(rowIndex));
                }
                @Override
                public void cell(String index, String data, XSSFComment xssfComment) {
                    int i = cellToIndex(index);
                    dataMap.put(i, data);
                }
                @Override
                public void endRow(int rowIndex) {
                    for (int cellIndex = 0; cellIndex <= endIndex[0]; cellIndex++) {
                        finalCellListener.then(new Cell(cellIndex, MapUtil.getValue(dataMap, cellIndex, String.class), new Row(rowIndex)));
                    }
                    finalRowEnd.then(new Row(rowIndex));
                }
            }, false));
            xmlReader.parse(new InputSource(inputStream));
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    private static int cellToIndex(String cell) {
        StringBuilder stringBuilder = new StringBuilder();
        String column = "";
        for (char c : cell.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                stringBuilder.append(c);
            } else {
                column = stringBuilder.toString();
            }
        }
        int result = 0;
        for (char c : column.toCharArray()) {
            result = result * 26 + (c - 'A') + 1;
        }
        return result - 1;
    }

}