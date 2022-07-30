package com.codejune.uiAuto;

/**
 * Alert
 *
 * @author ZJ
 * */
public final class Alert {

    private final org.openqa.selenium.Alert alert;

    public Alert(org.openqa.selenium.Alert alert) {
        this.alert = alert;
    }

    /**
     * 点击确定
     * */
    public void ok() {
        this.alert.accept();
    }

    /**
     * 点击取消
     * */
    public void cancel() {
        this.alert.dismiss();
    }

    /**
     * 获取弹框文本
     *
     * @return 弹框文本
     * */
    public String getText() {
        return this.alert.getText();
    }

}