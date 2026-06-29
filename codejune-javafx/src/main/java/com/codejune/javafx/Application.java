package com.codejune.javafx;

import javafx.stage.Stage;

public abstract class Application extends javafx.application.Application {

    private static final Window WINDOW = new Window();

    private static String icon;

    @Override
    public final void start(Stage stage) {
        this.initWindow(WINDOW);
        WINDOW.setStage(stage);
        WINDOW.open();
    }

    protected abstract void initWindow(Window window);

    public static Window getWindow() {
        return WINDOW;
    }

    public static void setIcon(String icon) {
        Application.icon = icon;
    }

    public static String getIcon() {
        return Application.icon;
    }

}