package com.codejune.uiAuto.webDriver;

import com.codejune.common.SystemOS;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.MapUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.util.Arrays;

/**
 * 谷歌驱动
 *
 * @author ZJ
 * */
public final class ChromeWebDriver extends BaseWebDriver {

    public ChromeWebDriver(File webDriverFile, boolean isShow) {
        super(getWebDriver(webDriverFile, isShow));
    }

    public ChromeWebDriver(File webDriverFile) {
        this(webDriverFile, false);
    }

    public ChromeWebDriver(String webDriverFilePath, boolean isShow) {
        this(new File(webDriverFilePath), isShow);
    }

    public ChromeWebDriver(String webDriverFilePath) {
        this(webDriverFilePath, false);
    }

    private static WebDriver getWebDriver(File webDriverFile, boolean isShow) {
        if (webDriverFile == null) {
            throw new InfoException("文件不能为空");
        }
        System.setProperty("webdriver.chrome.driver", webDriverFile.getAbsolutePath());

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("start-maximized");
        if (!isShow) {
            chromeOptions.addArguments("--headless=new");
        }
        if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
            chromeOptions.addArguments("no-sandbox");
        }
        chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation", "enable-logging"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        ChromeDriver chromeDriver;
        try {
            chromeDriver = new ChromeDriver(chromeOptions);
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", MapUtil.parse("{\"source\":\"Object.defineProperty(navigator, 'webdriver', {       get: () => undefined     })\"}", String.class, Object.class));
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
        return chromeDriver;
    }

}