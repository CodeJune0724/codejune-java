package com.codejune.javafx;

import javafx.stage.Stage;
import java.io.InputStream;

public abstract class Application extends javafx.application.Application {

    private static final Window WINDOW = new Window();

    private static InputStream icon;

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

    public static void setIcon(InputStream icon) {
        Application.icon = icon;
    }

    public static InputStream getIcon() {
        return Application.icon;
    }

}