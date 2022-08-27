package com.codejune;

import com.codejune.common.File;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.IOUtil;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.BaseElement;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * xml
 *
 * @author ZJ
 * */
public final class Xml {

    private final Document document;

    public Xml(String data) {
        SAXReader reader = new SAXReader();
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(data.getBytes());
            document = reader.read(byteArrayInputStream);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(byteArrayInputStream);
        }
    }

    public Xml(java.io.File file) {
        this(new File(file, File.FileType.FILE).getData());
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
     * 删除根元素
     * */
    public void deleteRootElement() {
        Element rootElement = this.getRootElement();
        if (rootElement != null) {
            this.document.remove(rootElement.element);
        }
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
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(stringWriter);
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        new File(file, File.FileType.FILE).setData(toString(isFormat));
    }

    public void save(java.io.File file) {
        save(file, true);
    }

    public static final class Element implements Iterable<Element> {

        private final org.dom4j.Element element;

        private Element(org.dom4j.Element element) {
            this.element = element;
        }

        /**
         * 获取element
         *
         * @param name 节点名
         *
         * @return Element
         * */
        public Element getElement(String name) {
            return new Element(this.element.element(name));
        }

        /**
         * 获取elements
         *
         * @return List
         * */
        public List<Element> getElements() {
            List<Element> elements = new ArrayList<>();
            for (org.dom4j.Element element : this.element.elements()) {
                if (element != null) {
                    elements.add(new Element(element));
                }
            }
            return elements;
        }

        /**
         * 获取元素文本
         *
         * @return 元素文本
         * */
        public String getText() {
            return this.element.getText();
        }

        /**
         * 获取元素属性
         *
         * @param name 属性名
         *
         * @return 属性值
         * */
        public String getAttribute(String name) {
            return this.element.attribute(name).getText();
        }

        /**
         * 获取节点名
         *
         * @return 节点名
         * */
        public String getName() {
            return this.element.getName();
        }

        /**
         * 设置元素文本
         *
         * @param text 文本
         * */
        public void setText(String text) {
            this.element.setText(text);
        }

        /**
         * 设置元素属性
         *
         * @param name 属性名
         * @param value 属性值
         * */
        public void setAttribute(String name, String value) {
            this.element.addAttribute(name, value);
        }

        /**
         * 设置节点名
         *
         * @param name 节点名
         * */
        public void setName(String name) {
            this.element.setName(name);
        }

        /**
         * 添加element
         *
         * @param name 节点名
         *
         * @return Element
         * */
        public Element addElement(String name) {
            return new Element(this.element.addElement(name));
        }

        /**
         * 删除属性
         *
         * @param name 属性名
         * */
        public void deleteAttribute(String name) {
            Attribute attribute = this.element.attribute(name);
            if (attribute != null) {
                this.element.remove(attribute);
            }
        }

        /**
         * 删除元素
         *
         * @param name 节点名
         * */
        public void deleteElement(String name) {
            org.dom4j.Element element = this.element.element(name);
            if (element != null) {
                this.element.remove(element);
            }
        }

        @Override
        public Iterator<Element> iterator() {
            List<Element> elements = getElements();
            final int[] i = {0};
            return new Iterator<Element>() {
                @Override
                public boolean hasNext() {
                    return i[0] < elements.size();
                }

                @Override
                public Element next() {
                    return elements.get(i[0]++);
                }
            };
        }

    }

}
