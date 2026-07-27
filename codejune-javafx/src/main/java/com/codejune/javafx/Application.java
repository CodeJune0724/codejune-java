package com.codejune.javafx;

import javafx.stage.Stage;
import java.io.InputStream;
import java.util.function.Supplier;

public abstract class Application extends javafx.application.Application {

    private static Window WINDOW = null;

    private static Supplier<InputStream> icon;

    @Override
    public final void start(Stage stage) {
        WINDOW = new Window();
        this.initWindow(WINDOW);
        WINDOW.setStage(stage);
        WINDOW.open();
    }

    protected abstract void initWindow(Window window);

    public static Window getWindow() {
        return WINDOW;
    }

    public static void setIcon(Supplier<InputStream> icon) {
        Application.icon = icon;
    }

    public static InputStream getIcon() {
        if (Application.icon == null) {
            return null;
        }
        return Application.icon.get();
    }

}