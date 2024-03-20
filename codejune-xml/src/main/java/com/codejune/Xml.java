package com.codejune;

import com.codejune.common.BaseException;
import com.codejune.common.os.File;
import com.codejune.common.util.FileUtil;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.xml.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.BaseElement;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * xml
 *
 * @author ZJ
 * */
public final class Xml {

    private final Document document;

    public Xml(String data) {
        if (StringUtil.isEmpty(data)) {
            throw new BaseException("xml data is null");
        }
        SAXReader reader = new SAXReader();
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(data.getBytes());
            document = reader.read(byteArrayInputStream);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            IOUtil.close(byteArrayInputStream);
        }
    }

    public Xml(java.io.File file) {
        this(FileUtil.exist(file) ? new File(file).getData() : null);
    }

    public Xml() {
        this.document = DocumentHelper.createDocument();
    }

    /**
     * 获取根元素
     *
     * @return 根元素
     * */
    public Element getRootElement() {
        org.dom4j.Element rootElement = this.document.getRootElement();
        if (rootElement == null) {
            return null;
        }
        return new Element(rootElement);
    }

    /**
     * 设置根元素
     *
     * @param rootElementName 根元素名称
     *
     * @return Element
     * */
    public Element setRootElement(String rootElementName) {
        Element rootElement = getRootElement();
        if (rootElement == null) {
            this.document.setRootElement(new BaseElement(rootElementName));
        } else {
            rootElement.setName(rootElementName);
        }
        return getRootElement();
    }

    /**
     * 获取xml所有的数据
     *
     * @param isFormat 是否格式化
     *
     * @return xml所有的数据
     * */
    public String toString(boolean isFormat) {
        OutputFormat outputFormat = OutputFormat.createPrettyPrint();
        outputFormat.setEncoding("UTF-8");
        outputFormat.setNewlines(true);
        outputFormat.setIndent("    ");
        StringWriter stringWriter = new StringWriter();
        XMLWriter writer;
        if (isFormat) {
            writer = new XMLWriter(stringWriter, outputFormat);
        } else {
            writer = new XMLWriter(stringWriter);
        }
        try {
            writer.write(this.document);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            IOUtil.close(stringWriter);
            try {
                writer.close();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * 保存
     *
     * @param file file
     * @param isFormat 是否格式化
     * */
    public void save(java.io.File file, boolean isFormat) {
        new File(file).write(toString(isFormat));
    }

    public void save(java.io.File file) {
        save(file, true);
    }

}