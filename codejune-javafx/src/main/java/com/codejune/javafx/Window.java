package com.codejune.javafx;

import com.codejune.core.util.ArrayUtil;
import com.codejune.javafx.component.BaseComponent;
import com.codejune.javafx.component.Tray;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Window {

    private final Stage stage;

    private Supplier<InputStream> icon;

    private String title;

    private int width;

    private int height;

    private boolean resizable = true;

    private Consumer<BaseComponent> render;

    private Runnable closeHandler = null;

    private Tray tray = null;

    public Window(Stage stage) {
        this.stage = stage;
        this.setIcon(Application.getIcon());
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

    public Stage getStage() {
        return this.stage;
    }

    public void setTitle(String title) {
        this.title = title;
        if (this.stage != null) {
            Platform.runLater(() -> this.stage.setTitle(this.title));
        }
    }

    public double getWidth() {
        return this.stage.getWidth();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public double getHeight() {
        return this.stage.getHeight();
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public Supplier<InputStream> getIcon() {
        return this.icon;
    }

    public void setIcon(Supplier<InputStream> icon) {
        this.icon = icon;
    }

    public void render(Consumer<BaseComponent> render) {
        this.render = render;
    }

    public void setCloseHandler(Runnable closeHandler) {
        if (closeHandler == null) {
            return;
        }
        this.closeHandler = closeHandler;
        if (this.stage != null) {
            this.stage.setOnCloseRequest(windowEvent -> {
                try {
                    this.closeHandler.run();
                } catch (Throwable e) {
                    windowEvent.consume();
                }
            });
        }
    }

    public void setPosition(double x, double y) {
        this.stage.setX(x);
        this.stage.setY(y);
    }

    public void open() {
        Platform.runLater(() -> {
            this.stage.setTitle(this.title);
            this.stage.setResizable(this.resizable);
            if (this.icon != null) {
                this.stage.getIcons().add(new Image(this.icon.get()));
            }
            if (this.closeHandler != null) {
                this.stage.setOnCloseRequest(windowEvent -> {
                    try {
                        this.closeHandler.run();
                    } catch (Throwable e) {
                        windowEvent.consume();
                    }
                });
            }
            this.renderBaseComponent(this.render, false);
            this.stage.show();
            this.stage.sizeToScene();
        });
    }

    public void close() {
        if (this.closeHandler != null) {
            try {
                this.closeHandler.run();
            } catch (Throwable e) {
                return;
            }
        }
        this.stage.close();
    }

    public void hide() {
        Platform.runLater(() -> {
            if (this.stage.isIconified()) {
                this.stage.setIconified(false);
            }
            this.stage.hide();
        });
    }

    public void show() {
        Platform.runLater(() -> {
            this.stage.show();
            this.stage.toFront();
            this.stage.requestFocus();
        });
    }

    public Tray tray() {
        if (this.tray == null) {
            this.tray = new Tray(this);
        }
        return this.tray;
    }

    public void reRender(Consumer<BaseComponent> render) {
        Platform.runLater(() -> renderBaseComponent(render, true));
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

    private void renderBaseComponent(Consumer<BaseComponent> render, boolean reload) {
        VBox vBox = new VBox();
        BaseComponent baseComponent = new BaseComponent() {
            @Override
            public Node getFxNode() {
                return vBox;
            }
        };
        baseComponent.setWindow(this);
        for (String sheet : ArrayUtil.asList("/javafx/style/base.css")) {
            baseComponent.getStyle().addSheet(sheet);
        }
        if (this.width > 0) {
            baseComponent.getStyle().setWidth(this.width);
        }
        if (this.height > 0) {
            baseComponent.getStyle().setHeight(this.height);
        }
        if (render != null) {
            render.accept(baseComponent);
        }
        if (!reload) {
            Scene scene = new Scene(vBox);
            this.stage.setScene(scene);
        } else {
            this.stage.getScene().setRoot(vBox);
        }
        if (!this.resizable) {
            this.autoSize(this.stage, vBox);
        }
    }

}