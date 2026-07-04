package com.codejune.core.os;

import com.codejune.core.BaseException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

/**
 * 系统
 *
 * @author ZJ
 * */
public final class System {

    /**
     * 打开网址
     *
     * @param url url
     * */
    public static void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                throw new BaseException("打开失败: " + url);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取剪贴板
     *
     * @return 剪贴板
     * */
    public static String getClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
            return null;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 复制到剪贴板
     *
     * @param value 需要复制的内容
     * */
    public static void copyToClipboard(String value) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(value);
        clipboard.setContents(selection, null);
    }

    /**
     * 获取屏幕宽高
     *
     * @return DisplayMode
     * */
    public static DisplayMode getScreen() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    }

}