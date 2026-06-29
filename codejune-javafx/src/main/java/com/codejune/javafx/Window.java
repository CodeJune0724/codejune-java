package com.codejune.javafx;

import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.javafx.component.BaseComponent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class Window {

    private Stage stage;

    private String title;

    private int width;

    private int height;

    private boolean resizable = true;

    private Consumer<BaseComponent> render;

    private InputStream icon;

    private Runnable closeHandler = null;

    public Window(Stage stage) {
        this.stage = stage;
    }

    public Window() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        this(stage);
    }

    public double getX() {
        return this.stage.getX();
    }

    public double getY() {
        return this.stage.getY();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public void setIcon(InputStream icon) {
        this.icon = icon;
    }

    public void render(Consumer<BaseComponent> render) {
        this.render = render;
        if (this.stage != null) {
            this.open();
        }
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void setPosition(double x, double y) {
        this.stage.setX(x);
        this.stage.setY(y);
    }

    public void open() {
        Platform.runLater(() -> {
            this.stage.setTitle(this.title);
            this.stage.setResizable(this.resizable);
            if (!StringUtil.isEmpty(Application.getIcon())) {
                this.stage.getIcons().add(new Image(Objects.requireNonNullElseGet(this.icon, () -> Objects.requireNonNull(getClass().getResourceAsStream(Application.getIcon())))));
            }
            if (this.closeHandler != null) {
                this.stage.setOnCloseRequest(_ -> this.closeHandler.run());
            }
            VBox vBox = new VBox();
            BaseComponent vBoxBaseComponent = new BaseComponent() {
                @Override
                public Node getFxNode() {
                    return vBox;
                }
            };
            for (String sheet : ArrayUtil.asList("/javafx/style/base.css")) {
                vBoxBaseComponent.getStyle().addSheet(sheet);
            }
            if (this.render != null) {
                this.render.accept(vBoxBaseComponent);
            }
            Scene scene = new Scene(vBox, this.width, this.height);
            this.stage.setScene(scene);
            if (!this.resizable) {
                this.autoSize(this.stage, vBox);
            }
            this.stage.show();
            this.stage.sizeToScene();
        });
    }

    public void close() {
        this.stage.close();
    }

    private void autoSize(Stage stage, VBox rootNode) {
        AtomicBoolean isProcessing = new AtomicBoolean(false);
        Runnable autoSizeTask = () -> {
            if (isProcessing.get()) return;
            isProcessing.set(true);
            Platform.runLater(() -> {
                try {
                    double sceneWidth = rootNode.getScene().getWidth();
                    double sceneHeight = rootNode.getScene().getHeight();
                    double stageWidth = stage.getWidth();
                    double stageHeight = stage.getHeight();
                    if (Math.abs(stageWidth - sceneWidth) > 0.1 || Math.abs(stageHeight - sceneHeight) > 0.1) {
                        stage.sizeToScene();
                    }
                } finally {
                    isProcessing.set(false);
                }
            });
        };
        rootNode.needsLayoutProperty().addListener((_, _, newVal) -> {
            if (newVal) {
                autoSizeTask.run();
            }
        });
        rootNode.layoutBoundsProperty().addListener((_, _, _) -> autoSizeTask.run());
    }

}