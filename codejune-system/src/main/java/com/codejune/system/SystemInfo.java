package com.codejune.system;

import java.awt.*;

/**
 * 系统信息
 *
 * @author ZJ
 * */
public final class SystemInfo {

    /**
     * 获取屏幕宽高
     *
     * @return DisplayMode
     * */
    public static DisplayMode getScreen() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    }

}