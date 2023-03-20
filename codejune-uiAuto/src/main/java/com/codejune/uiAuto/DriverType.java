package com.codejune.uiAuto;

import com.codejune.common.Action;
import com.codejune.common.SystemOS;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.util.Arrays;

/**
 * 驱动类型
 *
 * @author ZJ
 * */
public enum DriverType {

    /**
     * CHROME
     * */
    CHROME(isShow -> {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("start-maximized");
        if (!isShow) {
            chromeOptions.addArguments("--headless");
        }
        if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
            chromeOptions.addArguments("no-sandbox");
        }
        chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation", "enable-logging"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        chromeOptions.addArguments("--remote-allow-origins=*");
        return chromeOptions;
    }),

    /**
     * FIREBOX
     * */
    FIREBOX(isShow -> {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("start-maximized");
        if (!isShow) {
            firefoxOptions.addArguments("--headless");
        }
        if (SystemOS.getCurrentSystemOS() == SystemOS.LINUX) {
            firefoxOptions.addArguments("no-sandbox");
        }
        firefoxOptions.addArguments("--remote-allow-origins=*");
        return firefoxOptions;
    });

    private final Action<Boolean, MutableCapabilities> action;

    DriverType(Action<Boolean, MutableCapabilities> action) {
        this.action = action;
    }

    public MutableCapabilities getMutableCapabilities(boolean isShow) {
        return this.action.then(isShow);
    }

}