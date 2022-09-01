package com.codejune.uiAuto;

import org.openqa.selenium.Keys;
import java.util.List;

/**
 * WebElement元素
 *
 * @author ZJ
 * */
public interface WebElement {

    /**
     * 点击元素
     *
     * @param waitIsClick 是否等待至元素可点击，timeout：10s
     * */
    void click(boolean waitIsClick);

    /**
     * 点击元素
     * */
    void click();

    /**
     * 输入框输入参数
     *
     * @param value 要输入的参数
     * @param isClean 是否要清空
     * */
    void sendKey(String value, boolean isClean);

    /**
     * 输入框输入参数
     *
     * @param value 要输入的参数
     * */
    default void sendKey(String value) {
        sendKey(value, false);
    }

    /**
     * 输入框输入参数
     *
     * @param keys keys
     * */
    void sendKey(Keys keys);

    /**
     * 通过js输入
     *
     * @param value 要输入的参数
     * */
    void sendKeyByJs(String value);

    /**
     * 获取父级元素
     *
     * @return 返回父级元素
     * */
    WebElement getParent();

    /**
     * 获取元素属性值
     *
     * @param name 元素属性名
     *
     * @return 属性值
     * */
    String getAttribute(String name);

    /**
     * 获取子元素
     *
     * @param index 元素索引，从0开始
     *
     * @return 返回子元素
     * */
    WebElement getChild(int index);

    /**
     * 获取下一个元素
     *
     * @return 返回同级下一个元素，如果不存在，抛出元素未找到异常
     * */
    WebElement getNext();

    /**
     * 获取上一个元素
     *
     * @return 返回同级上一个元素，如果不存在，抛出元素未找到异常
     * */
    WebElement getPrev();

    /**
     * 获取子元素个数
     *
     * @return 子元素个数
     * */
    Integer getChildNumber();

    /**
     * 获取元素文本
     *
     * @return 元素文本
     * */
    String getText();

    /**
     * 元素是否存在
     *
     * @return 存在返回true
     * */
    boolean isDisplay();

    /**
     * 获取所有子元素
     *
     * @return 所有子元素
     * */
    List<WebElement> getChildren();

    /**
     * 清空输入内容
     * */
    void clear();

    /**
     * 触发事件
     *
     * @param event 事件名
     * */
    void event(String event);

    /**
     * 获取元素标签名
     *
     * @return 元素标签名
     * */
    String getTagName();

    /**
     * 获取selenium元素
     *
     * @return org.openqa.selenium.WebElement
     * */
    org.openqa.selenium.WebElement getSeleniumElement();

}