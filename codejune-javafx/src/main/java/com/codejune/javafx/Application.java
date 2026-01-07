package com.codejune.javafx;

import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Application
 * --module-path javafx\lib --add-modules=javafx.controls,javafx.fxml
 * */
public abstract class Application extends javafx.application.Application {

    private static final Window WINDOW = new Window();

    private static String icon;

    private static final List<String> sheetList = new ArrayList<>();

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

    public static void addSheet(String sheet) {
        Application.sheetList.add(sheet);
    }

    public static List<String> getSheet() {
        return Application.sheetList;
    }

}