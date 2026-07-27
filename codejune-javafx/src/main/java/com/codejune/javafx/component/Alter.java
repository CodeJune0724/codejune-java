package com.codejune.javafx.component;

import com.codejune.javafx.Window;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import java.util.function.Consumer;

public final class Alter {

    public static void info(String message, Window window) {
        baseAlter(Alert.AlertType.INFORMATION, "提示", message, window);
    }

    public static void error(String message, Window window) {
        baseAlter(Alert.AlertType.ERROR, "错误", message, window);
    }

    public static void confirm(String message, Consumer<Boolean> confirm, Window window) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(window.getStage());
            ButtonType yesButton = new ButtonType("确定");
            ButtonType noButton = new ButtonType("取消");
            alert.getButtonTypes().setAll(yesButton, noButton);
            Optional<ButtonType> buttonTypeOptional = alert.showAndWait();
            buttonTypeOptional.ifPresent(buttonType -> {
                if (confirm == null) {
                    return;
                }
                confirm.accept(buttonType == yesButton);
            });
        });
    }

    private static void baseAlter(Alert.AlertType alertType, String title, String message, Window window) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(window.getStage());
            alert.showAndWait();
        });
    }

}