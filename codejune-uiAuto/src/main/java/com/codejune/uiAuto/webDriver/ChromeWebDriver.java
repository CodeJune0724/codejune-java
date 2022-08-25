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

        // 返回的驱动程序
        ChromeDriver chromeDriver;
        // 谷歌设置
        ChromeOptions chromeOptions = new ChromeOptions();

        System.setProperty("webdriver.chrome.driver", webDriverFile.getAbsolutePath());

        // 设置浏览器最大化
        chromeOptions.addArguments("start-maximized");

        chromeOptions.setHeadless(!isShow);

        // linux系统设置沙盒模式
        if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
            chromeOptions.addArguments("no-sandbox");
        }

        // 防检测
        chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation", "enable-logging"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);

        // 实例化驱动
        try {
            chromeDriver = new ChromeDriver(chromeOptions);
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", MapUtil.parse("{\"source\":\"Object.defineProperty(navigator, 'webdriver', {       get: () => undefined     })\"}", String.class, Object.class));
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }

        return chromeDriver;
    }

}