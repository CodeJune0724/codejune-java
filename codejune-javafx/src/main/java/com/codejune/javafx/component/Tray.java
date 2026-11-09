package com.codejune.javafx.component;

import com.codejune.core.BaseException;
import com.codejune.javafx.Window;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 托盘
 *
 * @author ZJ
 * */
public final class Tray {

    private final Window window;

    private TrayIcon trayIcon = null;

    private String tooltip;

    private Runnable closeHandler;

    public Tray(Window window) {
        this.window = window;
    }

    /**
     * 设置tooltip
     *
     * @param tooltip tooltip
     * */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        if (this.isOpen()) {
            this.trayIcon.setToolTip(this.tooltip);
        }
    }

    /**
     * 退出回调
     *
     * @param closeHandler closeHandler
     * */
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    /**
     * 开启托盘
     * */
    public void open() {
        if (this.trayIcon != null) {
            return;
        }
        if (!SystemTray.isSupported()) {
            throw new BaseException("当前系统不支持托盘服务");
        }
        Image image = this.loadTrayImage();
        if (image == null) {
            return;
        }
        SystemTray systemTray = SystemTray.getSystemTray();
        this.trayIcon = new TrayIcon(image, this.tooltip);
        this.trayIcon.setImageAutoSize(true);
        PopupMenu popupMenu = new PopupMenu();
        MenuItem menuItem = new MenuItem("exit");
        menuItem.addActionListener((_) -> {
            try {
                if (this.closeHandler != null) {
                    this.closeHandler.run();
                }
            } finally {
                this.close();
            }
        });
        popupMenu.add(menuItem);
        this.trayIcon.setPopupMenu(popupMenu);
        this.trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Tray.this.window.show();
                }
            }
        });
        try {
            systemTray.add(this.trayIcon);
        } catch (Exception _) {}
        Platform.setImplicitExit(false);
    }

    /**
     * 是否开启
     *
     * @return 是否开启
     * */
    public boolean isOpen() {
        return this.trayIcon != null;
    }

    /**
     * 关闭托盘
     * */
    public void close() {
        if (!this.isOpen()) {
            return;
        }
        SystemTray.getSystemTray().remove(this.trayIcon);
        this.trayIcon = null;
    }

    /**
     * 加载图标
     *
     * @return Image
     * */
    private Image loadTrayImage() {
        if (this.window == null) {
            return null;
        }
        try {
            return ImageIO.read(this.window.getIcon().get()).getScaledInstance(16, 16, 4);
        } catch (Exception e) {
            return null;
        }
    }

}