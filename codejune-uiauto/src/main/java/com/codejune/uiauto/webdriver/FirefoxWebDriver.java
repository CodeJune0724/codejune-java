package com.codejune.uiauto.webdriver;

import com.codejune.common.BaseException;
import com.codejune.uiauto.DriverType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.io.File;

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
            throw new BaseException("文件不能为空");
        }
        System.setProperty("webdriver.gecko.driver", webDriverFile.getAbsolutePath());
        FirefoxOptions firefoxOptions = (FirefoxOptions) DriverType.FIREBOX.getMutableCapabilities(isShow);
        WebDriver result;
        try {
            result = new FirefoxDriver(firefoxOptions);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
        return result;
    }

}