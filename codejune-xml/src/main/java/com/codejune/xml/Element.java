package com.codejune.xml;

import org.dom4j.Attribute;
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
     * 获取elements
     *
     * @return List
     * */
    public List<Element> getElement() {
        List<Element> elements = new ArrayList<>();
        for (org.dom4j.Element element : this.element.elements()) {
            elements.add(new Element(element));
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

    @Override
    public Iterator<Element> iterator() {
        List<Element> elementList = getElement();
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

}