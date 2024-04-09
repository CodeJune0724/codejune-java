package com.codejune;

import com.codejune.common.BaseException;
import com.codejune.common.Closeable;
import com.codejune.common.io.reader.TextInputStreamReader;
import com.codejune.common.util.*;
import com.codejune.excelreader.Sheet;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 读excel
 *
 * @author ZJ
 * */
public final class ExcelReader implements Closeable, Iterable<Sheet> {

    private final OPCPackage opcPackage;

    private final XSSFReader xssfReader;

    private final SharedStrings sharedStrings;

    private final StylesTable stylesTable;

    private String tempPath = null;

    private final File file;

    public ExcelReader(String path) {
        if (!FileUtil.isFile(new File(path))) {
            throw new BaseException("excel文件不存在");
        }
        try {
            this.opcPackage = OPCPackage.open(path, PackageAccess.READ);
            this.xssfReader = new XSSFReader(opcPackage);
            this.sharedStrings = xssfReader.getSharedStringsTable();
            this.stylesTable = xssfReader.getStylesTable();
            this.file = new File(path);
        } catch (Exception e) {
            this.close();
            throw new BaseException(e.getMessage());
        }
    }

    public ExcelReader(File file) {
        this(file == null ? null : file.getAbsolutePath());
    }

    public ExcelReader(InputStream inputStream) {
        this(((Function<Object, File>) o -> {
            File result = new File(System.getProperty("java.io.tmpdir"), "ExcelReader-" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".xlsx");
            new com.codejune.common.os.File(result).write(inputStream);
            return result;
        }).apply(null));
    }

    /**
     * 获取sheet
     *
     * @param index index
     *
     * @return Sheet
     * */
    public Sheet getSheet(int index) {
        return new Sheet(index, this.xssfReader, this.sharedStrings, this.stylesTable, this.tempPath, this.file);
    }

    /**
     * 获取sheet
     *
     * @param sheetName sheetName
     *
     * @return Sheet
     * */
    public Sheet getSheet(String sheetName) {
        if (StringUtil.isEmpty(sheetName)) {
            return null;
        }
        try (InputStream workbookData = xssfReader.getWorkbookData()) {
            String sheetList = RegexUtil.find("<sheets>(.*?)</sheets>", new TextInputStreamReader(workbookData).getData(), 0);
            if (!RegexUtil.test("name=\"" + sheetName + "\"", sheetList)) {
                return null;
            }
            String sheetId = RegexUtil.find("<sheet name=\"" + sheetName + "\" sheetId=\"(.*?)\"", sheetList, 1);
            return getSheet(ObjectUtil.transform(sheetId, Integer.class) - 1);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 设置缓存目录
     *
     * @param tempPath tempPath
     * */
    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    @Override
    public void close() {
        if (opcPackage == null) {
            return;
        }
        try {
            opcPackage.close();
        } catch (Exception ignored) {}
    }

    @Override
    public Iterator<Sheet> iterator() {
        try {
            final int[] index = {-1};
            Iterator<InputStream> sheetsData = this.xssfReader.getSheetsData();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return sheetsData.hasNext();
                }

                @Override
                public Sheet next() {
                    index[0]++;
                    InputStream inputStream = null;
                    try {
                        inputStream = sheetsData.next();
                        return getSheet(index[0]);
                    } catch (Exception e) {
                        throw new BaseException(e);
                    } finally {
                        IOUtil.close(inputStream);
                    }
                }
            };
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}