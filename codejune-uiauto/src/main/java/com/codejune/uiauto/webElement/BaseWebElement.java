package com.codejune.uiauto.webElement;

import com.codejune.common.BaseException;
import com.codejune.uiauto.WebElement;
import com.codejune.uiauto.WebDriver;
import org.openqa.selenium.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 基础WebElement实现类
 *
 * @author ZJ
 * */
public final class BaseWebElement implements WebElement {

    private final WebDriver webDriver;

    private final org.openqa.selenium.WebElement seleniumElement;

    private final String jsDocument;

    public BaseWebElement(WebDriver webDriver, org.openqa.selenium.WebElement seleniumElement, String jsDocument) {
        this.webDriver = webDriver;
        this.seleniumElement = seleniumElement;
        this.jsDocument = jsDocument;
    }

    @Override
    public void click(boolean waitIsClick) {
        if (!waitIsClick) {
            baseClick();
            return;
        }
        long startTime = new Date().getTime() + 10000L;
        while (true) {
            long nowTime = new Date().getTime();
            if (startTime < nowTime) {
                throw new BaseException("元素点击超时");
            }
            try {
                baseClick();
                break;
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void click() {
        this.click(true);
    }

    @Override
    public void sendKey(String value, boolean isClean) {
        if (value == null) {
            value = "";
        }
        long time = new Date().getTime();
        while (true) {
            long nowTime = new Date().getTime();
            if (nowTime - time > 10000) {
                throw new BaseException("元素输入超时");
            }
            try {
                if (isClean) {
                    this.clear();
                }
                this.seleniumElement.sendKeys(value);
                this.webDriver.sleep(100);
                break;
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void sendKey(Keys keys) {
        try {
            this.seleniumElement.sendKeys(keys);
            this.webDriver.sleep(100);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public void sendKeyByJs(String value) {
        if (value == null) {
            value = "";
        }
        String js = jsDocument + ".value = '" + value + "';";
        webDriver.executeScript(js);
        this.webDriver.sleep(100);
    }

    @Override
    public WebElement getParent() {
        try {
            org.openqa.selenium.WebElement element = this.seleniumElement.findElement(By.xpath("./.."));
            return new BaseWebElement(this.webDriver, element, this.jsDocument + ".parentElement");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public String getAttribute(String name) {
        return this.seleniumElement.getAttribute(name);
    }

    @Override
    public WebElement getChild(int index) {
        return getChildren().get(index);
    }

    @Override
    public WebElement getNext() {
        try {
            org.openqa.selenium.WebElement element = this.seleniumElement.findElement(By.xpath("./following-sibling::*[1]"));
            return new BaseWebElement(this.webDriver, element, this.jsDocument + ".nextElementSibling");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public WebElement getPrev() {
        try {
            org.openqa.selenium.WebElement element = this.seleniumElement.findElement(By.xpath("./preceding-sibling::*[1]"));
            return new BaseWebElement(this.webDriver, element, this.jsDocument + ".previousElementSibling");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        if (this.seleniumElement == null) {
            return "NULL";
        }

        return this.seleniumElement.toString();
    }

    @Override
    public Integer getChildNumber() {
        return this.getChildren().size();
    }

    @Override
    public String getText() {
        try {
            return this.seleniumElement.getAttribute("innerText");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public boolean isDisplay() {
        try {
            return this.seleniumElement.isDisplayed();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public List<WebElement> getChildren() {
        try {
            List<WebElement> result = new ArrayList<>();
            List<org.openqa.selenium.WebElement> elements = this.seleniumElement.findElements(By.xpath("./child::*"));
            for (int i = 0 ; i < elements.size(); i++) {
                result.add(new BaseWebElement(this.webDriver, elements.get(i), this.jsDocument + ".children[" + i + "]"));
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public void clear() {
        try {
            this.seleniumElement.clear();
            this.webDriver.executeScript(this.jsDocument + ".value = \"\";");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public void event(String event) {
        String js = "let element = " + this.jsDocument + ";\n" +
                "let event = document.createEvent(\"HTMLEvents\");\n" +
                "event.initEvent(\"" + event + "\", false, true);\n" +
                "element.dispatchEvent(event);";
        webDriver.executeScript(js);
    }

    @Override
    public String getTagName() {
        return this.seleniumElement.getTagName();
    }

    @Override
    public org.openqa.selenium.WebElement getSeleniumElement() {
        return this.seleniumElement;
    }

    private void baseClick() {
        this.webDriver.executeScript("arguments[0].scrollIntoView();", seleniumElement);
        try {
            seleniumElement.click();
        } catch (Exception e) {
            try {
                this.event("click");
            } catch (Exception e1) {
                throw new BaseException(e1.getMessage());
            }
        }
    }

}