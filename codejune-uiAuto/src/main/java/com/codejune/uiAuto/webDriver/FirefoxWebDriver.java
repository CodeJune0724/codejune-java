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
        // 返回的驱动
        WebDriver result;
        // 火狐设置
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        // 火狐设置List
        List<String> options = new ArrayList<>();

        System.setProperty("webdriver.gecko.driver", webDriverFile.getAbsolutePath());

        firefoxOptions.setHeadless(!isShow);

        // linux开启沙盒
        if (SystemOS.currentSystemOS() == SystemOS.LINUX) {
            options.add("--no-sandbox");
        }

        firefoxOptions.addArguments(options);

        // 实例化驱动
        try {
            result = new FirefoxDriver(firefoxOptions);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }

        return result;
    }

}