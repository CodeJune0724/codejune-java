package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.io.writer.OutputStreamWriter;
import com.codejune.core.os.File;
import com.codejune.core.util.FileUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.xml.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
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
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.getBytes())) {
            this.document = reader.read(byteArrayInputStream);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    public Xml(java.io.File file) {
        this(FileUtil.exist(file) ? new File(file).getData() : null);
    }

    public Xml() {
        this.document = DocumentHelper.createDocument();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * 获取根元素
     *
     * @return 根元素
     * */
    public Element getRootElement() {
        org.dom4j.Element rootElement = this.document.getRootElement();
        if (rootElement == null) {
            throw new BaseException("rootElement is null");
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
        rootElement.setName(rootElementName);
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
        XMLWriter writer = null;
        try (StringWriter stringWriter = new StringWriter()) {
            if (isFormat) {
                OutputFormat outputFormat = OutputFormat.createPrettyPrint();
                outputFormat.setEncoding("UTF-8");
                outputFormat.setNewlines(true);
                outputFormat.setIndent("    ");
                writer = new XMLWriter(stringWriter, outputFormat);
            } else {
                writer = new XMLWriter(stringWriter);
            }
            writer.write(this.document);
            return stringWriter.toString();
        } catch (Exception exception) {
            throw new BaseException(exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 保存
     *
     * @param outputStream file
     * @param isFormat 是否格式化
     * */
    public void save(OutputStream outputStream, boolean isFormat) {
        new OutputStreamWriter(outputStream).write(this.toString(isFormat).getBytes());
    }

    /**
     * 保存
     *
     * @param outputStream file
     * */
    public void save(OutputStream outputStream) {
        this.save(outputStream, true);
    }

}