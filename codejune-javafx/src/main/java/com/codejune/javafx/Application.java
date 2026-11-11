package com.codejune.javafx;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.function.Supplier;

public abstract class Application extends javafx.application.Application {

    private static Window WINDOW = null;

    private static Supplier<InputStream> icon;

    @Override
    public final void start(Stage stage) {
        WINDOW = new Window(stage);
        this.initWindow(WINDOW);
        WINDOW.setIcon(Application.getIcon());
        WINDOW.open();
    }

    protected abstract void initWindow(Window window);

    public static Window getWindow() {
        return WINDOW;
    }

    public static void setIcon(Supplier<InputStream> icon) {
        Application.icon = icon;
    }

    public static Supplier<InputStream> getIcon() {
        return Application.icon;
    }

    public static void runLater(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) runnable.run();
        else Platform.runLater(runnable);
    }

    public static void asynchronousRun(Runnable runnable, boolean asynchronous, boolean runLater) {
        if (runnable == null) {
            return;
        }
        if (asynchronous) {
            if (runLater) {
                Thread.startVirtualThread(() -> Platform.runLater(runnable));
            } else {
                Thread.startVirtualThread(runnable);
            }
        } else {
            if (runLater) {
                Platform.runLater(runnable);
            } else {
                runnable.run();
            }
        }
    }

    public static void asynchronousRun(Runnable runnable, boolean asynchronous) {
        asynchronousRun(runnable, asynchronous, false);
    }

}