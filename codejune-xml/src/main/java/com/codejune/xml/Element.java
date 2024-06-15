package com.codejune.xml;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.StringUtil;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Element
 *
 * @author ZJ
 * */
public final class Element implements Iterable<Element> {

    final org.dom4j.Element element;

    public Element(org.dom4j.Element element) {
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
        org.dom4j.Element originElement = this.element.element(name);
        if (originElement == null) {
            return null;
        }
        return new Element(originElement);
    }

    /**
     * 获取element
     *
     * @param index index
     *
     * @return Element
     * */
    public Element getElement(int index) {
        List<Element> elementList = this.getElementList();
        if (index >= elementList.size()) {
            return null;
        }
        return elementList.get(index);
    }

    /**
     * 获取elements
     *
     * @return List
     * */
    public List<Element> getElementList() {
        List<Element> result = new ArrayList<>();
        for (org.dom4j.Element element : this.element.elements()) {
            result.add(new Element(element));
        }
        return result;
    }

    /**
     * 获取elements
     *
     * @param name name
     *
     * @return List
     * */
    public List<Element> getElementList(String name) {
        List<Element> result = new ArrayList<>();
        if (StringUtil.isEmpty(name)) {
            return result;
        }
        for (org.dom4j.Element element : this.element.elements(name)) {
            result.add(new Element(element));
        }
        return result;
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
     * 获取父级
     *
     * @return 父级
     * */
    public Element getParent() {
        return new Element(this.element.getParent());
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
        try {
            return new Element(this.element.addElement(name));
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 添加element
     *
     * @param name 节点名
     * @param index index
     *
     * @return Element
     * */
    public Element addElement(String name, int index) {
        try {
            org.dom4j.Element result = DocumentHelper.createElement(name);
            this.element.elements().add(index, result);
            return new Element(result);
        } catch (Exception e) {
            throw new BaseException(e);
        }
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
     * */
    public void delete() {
        if (element == null) {
            return;
        }
        if (element.isRootElement()) {
            element.getDocument().remove(element);
        } else {
            this.element.getParent().remove(element);
        }
    }

    /**
     * xpath查找
     *
     * @param xpath xpath
     *
     * @return List
     * */
    public List<Element> xpathList(String xpath) {
        List<Element> result = new ArrayList<>();
        for (Node node : this.element.selectNodes(xpath)) {
            if (node instanceof org.dom4j.Element nodeElement) {
                result.add(new Element(nodeElement));
            }
        }
        return result;
    }

    /**
     * xpath查找
     *
     * @param xpath xpath
     *
     * @return List
     * */
    public Element xpath(String xpath) {
        return ArrayUtil.get(this.xpathList(xpath), 0);
    }

    /**
     * 获取下一个元素
     *
     * @return 下一个元素
     * */
    public Element getNext() {
        return this.xpath("./following-sibling::*[1]");
    }

    /**
     * 获取上一个元素
     *
     * @return 上一个元素
     * */
    public Element getPrev() {
        return this.xpath("./preceding-sibling::*[1]");
    }

    @Override
    public Iterator<Element> iterator() {
        List<Element> elementList = getElementList();
        final int[] i = {0};
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i[0] < elementList.size();
            }

            @Override
            public Element next() {
                return elementList.get(i[0]++);
            }
        };
    }

    @Override
    public String toString() {
        return this.element.asXML();
    }

}