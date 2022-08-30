package com.codejune.uiAuto.webDriver;

import com.codejune.common.exception.InfoException;
import com.codejune.uiAuto.DriverType;
import org.openqa.selenium.WebDriver;
import java.net.URL;

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
            return new org.openqa.selenium.remote.RemoteWebDriver(new URL(url), driverType.getMutableCapabilities(isShow));
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}