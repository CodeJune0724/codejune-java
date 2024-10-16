package com.codejune.uiauto.webdriver;

import com.codejune.core.BaseException;
import com.codejune.uiauto.DriverType;
import org.openqa.selenium.WebDriver;
import java.net.URI;

/**
 * RemoteWebDriver
 *
 * @author ZJ
 * */
public final class RemoteWebDriver extends BaseWebDriver {

    public RemoteWebDriver(String url, DriverType driverType, boolean isShow) {
        super(webDriver(url, driverType, isShow));
    }

    public RemoteWebDriver(String url, DriverType driverType) {
        this(url, driverType, false);
    }

    private static WebDriver webDriver(String url, DriverType driverType, boolean isShow) {
        try {
            return new org.openqa.selenium.remote.RemoteWebDriver(URI.create(url).toURL(), driverType.getMutableCapabilities(isShow));
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}