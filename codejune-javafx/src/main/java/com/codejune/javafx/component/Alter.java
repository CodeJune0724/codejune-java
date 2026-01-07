package com.codejune.javafx.component;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public final class Alter {

    public static void info(String message) {
        baseAlter(Alert.AlertType.INFORMATION, "提示", message);
    }

    public static void error(String message) {
        baseAlter(Alert.AlertType.ERROR, "错误", message);
    }

    public static void confirm(String message, Runnable confirm) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText(message);
            ButtonType yesButton = new ButtonType("确定");
            ButtonType noButton = new ButtonType("取消");
            alert.getButtonTypes().setAll(yesButton, noButton);
            Optional<ButtonType> buttonTypeOptional = alert.showAndWait();
            buttonTypeOptional.ifPresent(buttonType -> {
                if (buttonType == yesButton) {
                    if (confirm != null) {
                        confirm.run();
                    }
                }
            });
        });
    }

    private static void baseAlter(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

}