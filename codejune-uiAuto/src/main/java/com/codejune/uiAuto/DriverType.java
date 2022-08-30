package com.codejune.uiAuto;

import com.codejune.common.SystemOS;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * 驱动类型
 *
 * @author ZJ
 * */
public enum DriverType {

    /**
     * CHROME
     * */
    CHROME((boolean isShow) -> {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (isShow) {
            chromeOptions.setHeadless(true);
        }
        if (SystemOS.currentSystemOS() == SystemOS.LINUX) {
            chromeOptions.addArguments("--no-sandbox");
        }
        return chromeOptions;
    }),

    /**
     * FIREBOX
     * */
    FIREBOX((boolean isShow) -> {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        if (isShow) {
            firefoxOptions.setHeadless(true);
        }
        if (SystemOS.currentSystemOS() == SystemOS.LINUX) {
            firefoxOptions.addArguments("--no-sandbox");
        }
        return firefoxOptions;
    });

    private final Option option;

    DriverType(Option option) {
        this.option = option;
    }

    public MutableCapabilities getMutableCapabilities(boolean isShow) {
        return this.option.getMutableCapabilities(isShow);
    }

    private interface Option {

        MutableCapabilities getMutableCapabilities(boolean isShow);

    }

}