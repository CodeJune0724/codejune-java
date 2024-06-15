package com.codejune.uiauto;

import com.codejune.core.Closeable;
import java.util.List;

/**
 * WebDriver
 *
 * @author ZJ
 */
public interface WebDriver extends Closeable {

    /**
     * 获取SeleniumWebDriver
     *
     * @return org.openqa.selenium.WebDriver
     * */
    org.openqa.selenium.WebDriver getSeleniumWebDriver();

    /**
     * 查找元素
     *
     * @param selector selector
     * @param millisecond millisecond
     *
     * @return WebElement
     * */
    List<WebElement> findElements(Selector selector, long millisecond);

    /**
     * 查找元素
     *
     * @param selector selector
     *
     * @return WebElement
     * */
    List<WebElement> findElements(Selector selector);

    /**
     * 查找元素
     *
     * @param selector selector
     * @param millisecond millisecond
     *
     * @return WebElement
     * */
    WebElement findElement(Selector selector, long millisecond);

    WebElement findElement(Selector selector);

    /**
     * 线程等待
     *
     * @param millisecond 要等待的毫秒数
     * */
    void sleep(int millisecond);

    /**
     * 打开网址
     *
     * @param url 要打开的网址
     * */
    void open(String url);

    @Override
    void close();

    /**
     * 执行脚本
     *
     * @param ags ags
     * */
    Object executeScript(String script, Object... ags);

    /**
     * 执行脚本
     * */
    Object executeScript(String script);

    /**
     * 切换iframe
     *
     * @param selector selector
     * */
    void switchIframe(Selector selector);

    /**
     * 切换iframe
     *
     * @param idOrName idOrName
     * */
    void switchIframe(String idOrName);

    /**
     * 切换回主文档
     * */
    void switchDefaultContent();

    /**
     * 获取弹框
     *
     * @return Alert
     * */
    Alert getAlert();

    /**
     * 等待元素出现
     *
     * @param selector selector
     * @param millisecond millisecond
     * */
    void display(Selector selector, long millisecond);

    /**
     * 等待元素出现
     *
     * @param selector selector
     * */
    void display(Selector selector);

    /**
     * 获取url
     *
     * @return url
     * */
    String getUrl();

    /**
     * 刷新浏览器
     * */
    void refresh();

    /**
     * 元素是否存在
     *
     * @param selector selector
     * @param millisecond millisecond
     *
     * @return 是否存在
     * */
    boolean isExist(Selector selector, long millisecond);

    /**
     * 元素是否存在
     *
     * @param selector selector
     *
     * @return 是否存在
     * */
    boolean isExist(Selector selector);

    /**
     * 滚动下拉条
     *
     * @param px 滚动距离
     * */
    void scrollDown(int px);

}