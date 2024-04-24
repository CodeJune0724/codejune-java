package com.codejune.excelreader;

import com.codejune.Xml;
import com.codejune.common.BaseException;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.*;
import com.codejune.excel.Image;
import com.codejune.xml.Element;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    private final String tempPath;

    private final File file;

    public Sheet(int index, XSSFReader xssfReader, SharedStrings sharedStrings, StylesTable stylesTable, String tempPath, File file) {
        this.index = index;
        this.xssfReader = xssfReader;
        this.sharedStrings = sharedStrings;
        this.stylesTable = stylesTable;
        this.tempPath = tempPath;
        this.file = file;
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
            throw new BaseException(e);
        }
    }

    /**
     * 读
     *
     * @param rowStart rowStart
     * @param cell cell
     * @param rowEnd rowEnd
     * */
    public void read(final Consumer<Row> rowStart, final Consumer<Cell> cell, final Consumer<Row> rowEnd) {
        Map<Integer, String> dataMap = new HashMap<>();
        try (InputStream inputStream = this.xssfReader.getSheet("rId" + (this.index + 1))) {
            XMLReader xmlReader = SAXParserFactory.newNSInstance().newSAXParser().getXMLReader();
            xmlReader.setContentHandler(new XSSFSheetXMLHandler(this.stylesTable, this.sharedStrings, new XSSFSheetXMLHandler.SheetContentsHandler() {
                @Override
                public void startRow(int rowIndex) {
                    dataMap.clear();
                    if (rowStart != null) {
                        rowStart.accept(new Row(rowIndex));
                    }
                }
                @Override
                public void cell(String index, String data, XSSFComment xssfComment) {
                    int i = cellToIndex(index);
                    dataMap.put(i, data);
                }
                @Override
                public void endRow(int rowIndex) {
                    Integer max = 0;
                    for (Integer item : dataMap.keySet()) {
                        if (item > max) {
                            max = item;
                        }
                    }

                    for (int cellIndex = 0; cellIndex <= max; cellIndex++) {
                        if (cell != null) {
                            cell.accept(new Cell(cellIndex, MapUtil.get(dataMap, cellIndex, String.class), new Row(rowIndex)));
                        }
                    }
                    if (rowEnd != null) {
                        rowEnd.accept(new Row(rowIndex));
                    }
                }
            }, false));
            xmlReader.parse(new InputSource(inputStream));
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 读图片
     *
     * @param consumer consumer
     * */
    public void readFile(Consumer<Image> consumer) {
        if (consumer == null) {
            return;
        }
        if (!FileUtil.isFile(this.file)) {
            throw new BaseException("no file");
        }
        if (StringUtil.isEmpty(this.tempPath)) {
            throw new BaseException("tempPath is null");
        }
        com.codejune.common.os.File copFile = new com.codejune.common.os.File(this.file).copy(this.tempPath);
        File zipFile = new File(copFile.getPath() + ".zip");
        FileUtil.delete(zipFile);
        copFile.rename(zipFile.getName());
        ZipUtil.unzip(new File(zipFile.getPath()), this.tempPath);
        Xml xml = new Xml(new File(this.tempPath, "xl/drawings/drawing" + (this.index + 1) + ".xml"));
        List<Element> relationshipList = new Xml(new File(this.tempPath, "xl/drawings/_rels/drawing" + (this.index + 1) + ".xml.rels")).getRootElement().getElementList();
        for (Element twoCellAnchor : xml.getRootElement().getElementList()) {
            Element from = twoCellAnchor.getElement("from");
            if (from == null) {
                continue;
            }
            Element row = from.getElement("row");
            Element col = from.getElement("col");
            if (row == null) {
                continue;
            }
            if (col == null) {
                continue;
            }
            int rowIndex = ObjectUtil.transform(row.getText(), Integer.class);
            int cellIndex = ObjectUtil.transform(col.getText(), Integer.class);
            Element pic = twoCellAnchor.getElement("pic");
            if (pic == null) {
                continue;
            }
            Element blipFill = pic.getElement("blipFill");
            if (blipFill == null) {
                continue;
            }
            Element blip = blipFill.getElement("blip");
            if (blip == null) {
                continue;
            }
            String embed = blip.getAttribute("embed");
            if (StringUtil.isEmpty(embed)) {
                continue;
            }
            File imageFile = null;
            for (Element relationship : relationshipList) {
                if (embed.equals(relationship.getAttribute("Id"))) {
                    imageFile = new File(this.tempPath + "/xl/drawings", relationship.getAttribute("Target"));
                }
            }
            if (!FileUtil.isFile(imageFile)) {
                continue;
            }
            try (InputStream inputStream = IOUtil.getInputStream(imageFile)) {
                consumer.accept(new Image(rowIndex, cellIndex, new com.codejune.common.os.File(imageFile).getSuffix(), inputStream));
            } catch (Exception e) {
                throw new BaseException(e);
            }
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