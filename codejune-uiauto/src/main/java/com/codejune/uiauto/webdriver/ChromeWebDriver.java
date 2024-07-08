package com.codejune.uiauto.webdriver;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.uiauto.DriverType;
import com.codejune.uiauto.http.HttpRequest;
import com.codejune.uiauto.http.HttpResponse;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v85.network.Network;
import org.openqa.selenium.devtools.v85.network.model.Request;
import org.openqa.selenium.devtools.v85.network.model.RequestId;
import org.openqa.selenium.devtools.v85.network.model.Response;
import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * 谷歌驱动
 *
 * @author ZJ
 * */
public final class ChromeWebDriver extends BaseWebDriver {

    private final Map<String, HttpRequest> requestIdMap = new HashMap<>();

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
            throw new BaseException("文件不能为空");
        }
        System.setProperty("webdriver.chrome.driver", webDriverFile.getAbsolutePath());
        ChromeOptions chromeOptions = (ChromeOptions) DriverType.CHROME.getMutableCapabilities(isShow);
        chromeOptions.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation", "enable-logging"));
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        ChromeDriver chromeDriver;
        try {
            chromeDriver = new ChromeDriver(chromeOptions);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
        return chromeDriver;
    }

    /**
     * 监控网络请求
     *
     * @param consumer consumer
     * */
    public void listenNetWork(BiConsumer<HttpRequest, HttpResponse> consumer) {
        if (consumer == null) {
            return;
        }
        DevTools devTools = ((ChromeDriver) this.getSeleniumWebDriver()).getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.addListener(Network.requestWillBeSent(), requestWillBeSent -> {
            synchronized (requestIdMap) {
                Request request = requestWillBeSent.getRequest();
                HttpRequest httpRequest = new HttpRequest();
                httpRequest.setUrl(request.getUrl());
                httpRequest.setType(request.getMethod());
                httpRequest.setHeader(new LinkedHashMap<>());
                for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
                    httpRequest.getHeader().put(entry.getKey(), ObjectUtil.toString(entry.getValue()));
                }
                httpRequest.setBody(request.getPostData().orElse(null));
                requestIdMap.put(requestWillBeSent.getRequestId().toString(), httpRequest);
            }
        });
        devTools.addListener(Network.responseReceived(), responseReceived -> {
            synchronized (requestIdMap) {
                RequestId requestId = responseReceived.getRequestId();
                HttpRequest httpRequest = requestIdMap.get(requestId.toString());
                Response response = responseReceived.getResponse();
                HttpResponse httpResponse = new HttpResponse();
                httpResponse.setStatus(response.getStatus());
                httpResponse.setHeader(new LinkedHashMap<>());
                for (Map.Entry<String, Object> entry : response.getHeaders().entrySet()) {
                    httpResponse.getHeader().put(entry.getKey(), ObjectUtil.toString(entry.getValue()));
                }
                httpResponse.setBody(devTools.send(Network.getResponseBody(requestId)).getBody());
                consumer.accept(httpRequest, httpResponse);
            }
        });
    }

}