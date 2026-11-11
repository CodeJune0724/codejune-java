package com.codejune.javafx;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.javafx.component.BaseComponent;
import com.codejune.javafx.component.Tray;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
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

    private String title;

    private double x;

    private double y;

    private int width;

    private int height;

    private boolean resizable = true;

    private Supplier<InputStream> icon;

    private Runnable closeHandler = null;

    private boolean isOpen = false;

    private boolean isListener = false;

    private Tray tray = null;

    private VBox autoSizeRoot;

    private ChangeListener<Boolean> autoSizeNeedsLayoutListener;

    private ChangeListener<Bounds> autoSizeLayoutBoundsListener;

    public Window(Stage stage) {
        if (stage == null) {
            throw new BaseException("stage is null");
        }
        this.stage = stage;
        this.setIcon(Application.getIcon());
    }

    public Window() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        this(stage);
    }

    public Stage getStage() {
        return this.stage;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        Application.runLater(() -> this.stage.setTitle(title));
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setPosition(double x, double y) {
        Application.runLater(() -> {
            this.stage.setX(x);
            this.stage.setY(y);
        });
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        Application.runLater(() -> this.stage.setWidth(width));
    }

    public void setMaxWidth(int width) {
        Application.runLater(() -> this.stage.setMaxWidth(width >= 0 ? width : Double.MAX_VALUE));
    }

    public void setMinWidth(int width) {
        Application.runLater(() -> this.stage.setMinWidth(Math.max(width, 0)));
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        Application.runLater(() -> this.stage.setHeight(height));
    }

    public void setMaxHeight(int height) {
        Application.runLater(() -> this.stage.setMaxHeight(height >= 0 ? height : Double.MAX_VALUE));
    }

    public void setMinHeight(int height) {
        Application.runLater(() -> this.stage.setMinHeight(Math.max(height, 0)));
    }

    public boolean isResizable() {
        return this.resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        Application.runLater(() -> {
            this.stage.setResizable(this.resizable);
            if (this.resizable) {
                removeAutoSize();
            } else {
                Scene scene = this.stage.getScene();
                if (scene != null && scene.getRoot() instanceof VBox vbox) {
                    this.autoSize(this.stage, vbox);
                    if (this.stage.isShowing()) {
                        this.stage.sizeToScene();
                    }
                }
            }
        });
    }

    public Supplier<InputStream> getIcon() {
        return this.icon;
    }

    public void setIcon(Supplier<InputStream> icon) {
        this.icon = icon;
        if (this.icon != null) {
            InputStream inputStream = this.icon.get();
            if (inputStream == null) {
                return;
            }
            Application.runLater(() -> this.stage.getIcons().setAll(new Image(inputStream)));
        }
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
        Application.runLater(() -> {
            if (this.closeHandler == null) {
                this.stage.setOnCloseRequest(null);
            } else {
                this.stage.setOnCloseRequest(windowEvent -> {
                    try {
                        this.closeHandler.run();
                    } catch (Throwable e) {
                        windowEvent.consume();
                    }
                });
            }
        });
    }

    public void render(Consumer<BaseComponent> render) {
        Application.runLater(() -> {
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
            if (this.stage.getScene() == null) {
                Scene scene = new Scene(vBox);
                this.stage.setScene(scene);
            } else {
                this.stage.getScene().setRoot(vBox);
            }
            this.removeAutoSize();
            if (!this.resizable) {
                this.autoSize(this.stage, vBox);
            }
            if (render != null) {
                render.accept(baseComponent);
            }
        });
    }

    public void open() {
        Application.runLater(() -> {
            if (this.isOpen) {
                this.stage.show();
                this.stage.toFront();
                return;
            }
            if (!this.isListener) {
                this.title = this.stage.getTitle();
                this.x = this.stage.getX();
                this.y = this.stage.getY();
                this.width = (int) this.stage.getWidth();
                this.height = (int) this.stage.getHeight();
                this.stage.titleProperty().addListener((_, _, title) -> this.title = title);
                this.stage.xProperty().addListener((_, _, x) -> this.x = x.doubleValue());
                this.stage.yProperty().addListener((_, _, y) -> this.y = y.doubleValue());
                this.stage.widthProperty().addListener((_, _, width) -> this.width = width.intValue());
                this.stage.heightProperty().addListener((_, _, height) -> this.height = height.intValue());
                this.isListener = true;
            }
            if (this.stage.getScene() == null) {
                return;
            }
            if (!this.isResizable()) {
                this.sizeToScene();
            }
            this.stage.show();
            this.isOpen = true;
        });
    }

    public void close() {
        Application.runLater(() -> {
            if (this.closeHandler != null) {
                try {
                    this.closeHandler.run();
                } catch (Throwable e) {
                    return;
                }
            }
            this.removeAutoSize();
            this.stage.close();
        });
    }

    public void hide() {
        Application.runLater(() -> {
            if (this.stage.isIconified()) {
                this.stage.setIconified(false);
            }
            this.stage.hide();
        });
    }

    public void show() {
        Application.runLater(() -> {
            this.stage.show();
            this.stage.toFront();
            this.stage.requestFocus();
        });
    }

    public void sizeToScene() {
        this.stage.sizeToScene();
    }

    public synchronized Tray tray() {
        if (this.tray == null) {
            this.tray = new Tray(this);
        }
        return this.tray;
    }

    private void autoSize(Stage stage, VBox rootNode) {
        this.removeAutoSize();
        AtomicBoolean isProcessing = new AtomicBoolean(false);
        Runnable autoSizeTask = () -> {
            if (isProcessing.get()) return;
            isProcessing.set(true);
            Platform.runLater(() -> {
                try {
                    Scene scene = rootNode.getScene();
                    if (scene == null) return;
                    double sceneWidth = scene.getWidth();
                    double sceneHeight = scene.getHeight();
                    if (Double.isNaN(sceneWidth) || Double.isNaN(sceneHeight)) return;
                    double stageWidth = stage.getWidth();
                    double stageHeight = stage.getHeight();
                    if (Double.isNaN(stageWidth) || Double.isNaN(stageHeight)) return;
                    if (Math.abs(stageWidth - sceneWidth) > 0.1 || Math.abs(stageHeight - sceneHeight) > 0.1) {
                        stage.sizeToScene();
                    }
                } finally {
                    isProcessing.set(false);
                }
            });
        };
        ChangeListener<Boolean> needsLayoutListener = (_, _, newVal) -> {
            if (newVal) autoSizeTask.run();
        };
        ChangeListener<Bounds> layoutBoundsListener = (_, _, _) -> autoSizeTask.run();
        rootNode.needsLayoutProperty().addListener(needsLayoutListener);
        rootNode.layoutBoundsProperty().addListener(layoutBoundsListener);
        this.autoSizeRoot = rootNode;
        this.autoSizeNeedsLayoutListener = needsLayoutListener;
        this.autoSizeLayoutBoundsListener = layoutBoundsListener;
    }

    private void removeAutoSize() {
        if (this.autoSizeRoot != null) {
            if (this.autoSizeNeedsLayoutListener != null) {
                this.autoSizeRoot.needsLayoutProperty().removeListener(this.autoSizeNeedsLayoutListener);
                this.autoSizeNeedsLayoutListener = null;
            }
            if (this.autoSizeLayoutBoundsListener != null) {
                this.autoSizeRoot.layoutBoundsProperty().removeListener(this.autoSizeLayoutBoundsListener);
                this.autoSizeLayoutBoundsListener = null;
            }
            this.autoSizeRoot = null;
        }
    }

}