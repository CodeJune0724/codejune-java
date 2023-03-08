package com.codejune.uiAuto.webDriver;

import com.codejune.common.SystemOS;
import com.codejune.common.exception.InfoException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 火狐驱动
 *
 * @author ZJ
 * */
public final class FirefoxWebDriver extends BaseWebDriver {

    public FirefoxWebDriver(File webDriverFile, boolean isShow) {
        super(getWebDriver(webDriverFile, isShow));
    }

    public FirefoxWebDriver(File webDriverFile) {
        this(webDriverFile, false);
    }

    public FirefoxWebDriver(String webDriverFilePath, boolean isShow) {
        this(new File(webDriverFilePath), isShow);
    }

    public FirefoxWebDriver(String webDriverFilePath) {
        this(webDriverFilePath, false);
    }

    private static WebDriver getWebDriver(File webDriverFile, boolean isShow) {
        if (webDriverFile == null) {
            throw new InfoException("文件不能为空");
        }
        System.setProperty("webdriver.gecko.driver", webDriverFile.getAbsolutePath());

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        List<String> options = new ArrayList<>();
        if (!isShow) {
            firefoxOptions.addArguments("--headless=new");
        }
        if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
            options.add("--no-sandbox");
        }
        firefoxOptions.addArguments(options);
        WebDriver result;
        try {
            result = new FirefoxDriver(firefoxOptions);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
        return result;
    }

}