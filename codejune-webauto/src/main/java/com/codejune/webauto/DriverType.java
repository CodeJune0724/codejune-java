package com.codejune.webauto;

import com.codejune.core.os.OSType;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.util.Arrays;
import java.util.function.Function;

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
        if (OSType.getCurrentOSType() == OSType.LINUX) {
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
        if (OSType.getCurrentOSType() == OSType.LINUX) {
            firefoxOptions.addArguments("no-sandbox");
        }
        firefoxOptions.addArguments("--remote-allow-origins=*");
        return firefoxOptions;
    });

    private final Function<Boolean, MutableCapabilities> action;

    DriverType(Function<Boolean, MutableCapabilities> action) {
        this.action = action;
    }

    public MutableCapabilities getMutableCapabilities(boolean isShow) {
        return this.action.apply(isShow);
    }

}