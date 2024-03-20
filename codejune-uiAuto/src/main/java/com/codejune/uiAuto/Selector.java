package com.codejune.uiAuto;

import com.codejune.common.BaseException;
import com.codejune.uiAuto.webElement.BaseWebElement;
import org.openqa.selenium.By;
import java.util.ArrayList;
import java.util.List;

/**
 * 元素选择器
 *
 * @author ZJ
 * */
public abstract class Selector {

    private final String select;

    private Selector(String select) {
        this.select = select;
    }

    public String getSelect() {
        return select;
    }

    /**
     * 获取元素
     *
     * @param webDriver webDriver
     *
     * @return List
     * */
    public abstract List<WebElement> getWebElements(WebDriver webDriver);

    /**
     * 通过CssSelector查找
     *
     * @param select select
     *
     * @return 元素选择器
     * */
    public static Selector byCssSelector(String select) {
        return new Selector(select) {
            @Override
            public List<WebElement> getWebElements(WebDriver webDriver) {
                try {
                    List<WebElement> result = new ArrayList<>();
                    List<org.openqa.selenium.WebElement> elements = webDriver.getSeleniumWebDriver().findElements(By.cssSelector(this.getSelect()));
                    for (int i = 0; i < elements.size(); i++) {
                        result.add(new BaseWebElement(webDriver, elements.get(i), "document.querySelectorAll(\"" + this.getSelect() + "\")[" + i + "]"));
                    }
                    return result;
                } catch (Exception e) {
                    throw new BaseException(e.getMessage());
                }
            }
        };
    }

    /**
     * 通过xpath查找
     *
     * @param select select
     *
     * @return 元素选择器
     * */
    public static Selector byXpath(String select) {
        return new Selector(select) {
            @Override
            public List<WebElement> getWebElements(WebDriver webDriver) {
                try {
                    List<WebElement> result = new ArrayList<>();
                    List<org.openqa.selenium.WebElement> elements = webDriver.getSeleniumWebDriver().findElements(By.xpath(this.getSelect()));
                    for (int i = 0; i < elements.size(); i++) {
                        String s = "result = elements.iterateNext();".repeat((i + 1));
                        result.add(new BaseWebElement(webDriver, elements.get(i), "(function() {let elements = document.evaluate('" + this.getSelect() + "', document);let result = null;" + s + "return result;})()"));
                    }
                    return result;
                } catch (Exception e) {
                    throw new BaseException(e.getMessage());
                }
            }
        };
    }

    /**
     * 通过文本查找
     *
     * @param select select
     *
     * @return 元素选择器
     * */
    public static Selector byText(String select) {
        return new Selector(select) {
            @Override
            public List<WebElement> getWebElements(WebDriver webDriver) {
                try {
                    List<WebElement> result = new ArrayList<>();
                    String text = "//*[contains(text(), \"" + this.getSelect() + "\")]";
                    List<org.openqa.selenium.WebElement> elements = webDriver.getSeleniumWebDriver().findElements(By.xpath(text));
                    for (int i = 0; i < elements.size(); i++) {
                        org.openqa.selenium.WebElement webElement = elements.get(i);
                        if ("script".equals(webElement.getTagName())) {
                            continue;
                        }
                        String s = "result = elements.iterateNext();".repeat((i + 1));
                        result.add(new BaseWebElement(webDriver, elements.get(i), "(function() {let elements = document.evaluate('//*[contains(text(), \"" + this.getSelect() + "\")]', document);let result = null;" + s + "return result;})()"));
                    }
                    return result;
                } catch (Exception e) {
                    throw new BaseException(e.getMessage());
                }
            }
        };
    }

}