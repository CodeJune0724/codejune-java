package com.codejune.uiAuto.webDriver;

import com.codejune.common.exception.InfoException;
import com.codejune.uiAuto.Alert;
import com.codejune.uiAuto.WebElement;
import com.codejune.uiAuto.Selector;
import org.openqa.selenium.JavascriptExecutor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AbstractWebDriver
 *
 * @author ZJ
 * */
public abstract class BaseWebDriver implements com.codejune.uiAuto.WebDriver {

    private org.openqa.selenium.WebDriver seleniumWebDriver;

    protected BaseWebDriver(org.openqa.selenium.WebDriver webDriver) {
        this.seleniumWebDriver = webDriver;
        this.seleniumWebDriver.manage().window().maximize();
    }

    @Override
    public final org.openqa.selenium.WebDriver getSeleniumWebDriver() {
        return seleniumWebDriver;
    }

    @Override
    public final List<WebElement> findElements(Selector selector, long millisecond) {
        return findElements(selector, millisecond, System.currentTimeMillis());
    }

    @Override
    public final List<WebElement> findElements(Selector selector) {
        return findElements(selector, 10000);
    }

    @Override
    public WebElement findElement(Selector selector, long millisecond) {
        return findElements(selector, millisecond).get(0);
    }

    @Override
    public final WebElement findElement(Selector selector) {
        return findElement(selector, 10000);
    }

    @Override
    public final void sleep(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public final void open(String url) {
        try {
            this.seleniumWebDriver.get(url);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public final void close() {
        if (this.seleniumWebDriver != null) {
            this.seleniumWebDriver.quit();
            this.seleniumWebDriver = null;
        }
    }

    @Override
    public final void executeScript(String script) {
        JavascriptExecutor js = (JavascriptExecutor) this.seleniumWebDriver;
        js.executeScript(script);
    }

    @Override
    public final void switchIframe(Selector selector) {
        this.seleniumWebDriver = this.seleniumWebDriver.switchTo().frame(findElement(selector).getSeleniumElement());
    }

    @Override
    public void switchIframe(String idOrName) {
        this.seleniumWebDriver = this.seleniumWebDriver.switchTo().frame(idOrName);
    }

    @Override
    public final void switchDefaultContent() {
        this.seleniumWebDriver = this.seleniumWebDriver.switchTo().defaultContent();
    }

    @Override
    public final Alert getAlert() {
        try {
            return new Alert(this.seleniumWebDriver.switchTo().alert());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void display(Selector selector, long millisecond) {
        try {
            findElements(selector, millisecond);
        } catch (Exception e) {
            throw new InfoException("元素等待超时: " + selector.getSelect());
        }
    }

    @Override
    public void display(Selector selector) {
        display(selector, 60000);
    }

    @Override
    public String getUrl() {
        return this.seleniumWebDriver.getCurrentUrl();
    }

    @Override
    public void refresh() {
        this.seleniumWebDriver.navigate().refresh();
    }

    @Override
    public boolean isExist(Selector selector, long millisecond) {
        try {
            findElement(selector, millisecond);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean isExist(Selector selector) {
        return isExist(selector, 10000);
    }

    private List<WebElement> findElements(Selector selector, long millisecond, long startTime) {
        try {
            this.seleniumWebDriver.manage().timeouts().implicitlyWait(Duration.ofMillis(millisecond));
            List<WebElement> webElements;
            try {
                webElements = selector.getWebElements(this);
            } catch (Exception e) {
                webElements = new ArrayList<>();
            }
            if (webElements.size() == 0) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - startTime > millisecond) {
                    throw new InfoException("元素未找到：" + selector.getSelect());
                }
                return findElements(selector, millisecond, startTime);
            }
            return webElements;
        } catch (Throwable e) {
            throw new InfoException(e.getMessage());
        }
    }

}