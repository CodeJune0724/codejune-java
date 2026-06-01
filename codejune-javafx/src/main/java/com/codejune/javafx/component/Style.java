package com.codejune.javafx.component;

import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.StringUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Style {

    private final BaseComponent baseComponent;

    public Style(BaseComponent baseComponent) {
        this.baseComponent = baseComponent;
    }

    public Style addSheet(URL url) {
        if (url == null) {
            return this;
        }
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Parent parent) {
            parent.getStylesheets().add(url.toExternalForm());
        }
        return this;
    }

    public Style addSheet(String path) {
        return this.addSheet(getClass().getResource(path));
    }

    public Style addClass(String className) {
        Node fxNode = this.baseComponent.getFxNode();
        fxNode.getStyleClass().add(className);
        return this;
    }

    public Style deleteClass(String className) {
        Node fxNode = this.baseComponent.getFxNode();
        fxNode.getStyleClass().remove(className);
        return this;
    }

    public Style addCss(String cssStyle) {
        if (StringUtil.isEmpty(cssStyle)) {
            return this;
        }
        if (cssStyle.endsWith(";")) {
            cssStyle = cssStyle.substring(0, cssStyle.length() - 1);
        }
        Node fxNode = this.baseComponent.getFxNode();
        List<String> cssStyleList = new ArrayList<>();
        String fxNodeStyle = fxNode.getStyle();
        if (!StringUtil.isEmpty(fxNodeStyle)) {
            cssStyleList.addAll(ArrayUtil.asList(fxNodeStyle.split(";")));
        }
        String finalCssStyle = cssStyle;
        cssStyleList.removeIf(item -> item.startsWith(finalCssStyle.split(":")[0]));
        cssStyleList.add(cssStyle);
        this.baseComponent.getFxNode().setStyle(ArrayUtil.toString(cssStyleList, s -> s, ";"));
        return this;
    }

    public Style setBackgroundColor(int red, int green, int blue) {
        return this.addCss("-fx-background-color: rgb(" + red + "," + green + "," + blue + ");");
    }

    public Style setBackgroundColor(String color) {
        return this.addCss("-fx-background-color: " + color);
    }

    public Style setFontSize(int size) {
        this.addCss("-fx-font-size: " + size + "px");
        return this;
    }

    public Style setBorderColor(String color) {
        this.addCss("-fx-border-color: " + color);
        return this;
    }

    public Style setFontWeight(String fontWeight) {
        this.addCss("-fx-font-weight: " + fontWeight);
        return this;
    }

    public Style setColor(String color) {
        this.addCss("-fx-text-fill: " + color);
        return this;
    }

    public Style setMargin(int top, int right, int bottom, int left) {
        Node fxNode = this.baseComponent.getFxNode();
        Parent parent = fxNode.getParent();
        if (parent == null) {
            return this;
        }
        if (parent instanceof VBox) {
            VBox.setMargin(fxNode, new Insets(top, right, bottom, left));
        }
        if (parent instanceof HBox) {
            HBox.setMargin(fxNode, new Insets(top, right, bottom, left));
        }
        return this;
    }

    public Style setPadding(int top, int right, int bottom, int left) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Pane pane) {
            pane.setPadding(new Insets(top, right, bottom, left));
        }
        return this;
    }

    public int getWidth() {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            return (int) region.getPrefWidth();
        }
        return 0;
    }

    public int getHeight() {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            return (int) region.getPrefHeight();
        }
        return 0;
    }

    public Style setWidth(int width) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            region.setPrefWidth(width);
        }
        return this;
    }

    public Style setHeight(int height) {
        this.addCss("-fx-pref-height: " + height + "px");
        return this;
    }

    public Style setMaxWidth(int width) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            region.setMaxWidth(width);
        }
        return this;
    }

    public Style setMaxHeight(int height) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            region.setMaxHeight(height);
        }
        return this;
    }

    public Style setMinWidth(int width) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            region.setMinWidth(width);
        }
        return this;
    }

    public Style setMinHeight(int height) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof Region region) {
            region.setMinHeight(height);
        }
        return this;
    }

    public Style maxWidth() {
        Node fxNode = this.baseComponent.getFxNode();
        Parent parent = fxNode.getParent();
        if (parent == null) {
            return this;
        }
        if (parent instanceof VBox) {
            VBox.setVgrow(fxNode, Priority.ALWAYS);
        }
        if (parent instanceof HBox) {
            HBox.setHgrow(fxNode, Priority.ALWAYS);
        }
        if (fxNode instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return this;
    }

    public Style maxHeight() {
        Node fxNode = this.baseComponent.getFxNode();
        Parent parent = fxNode.getParent();
        if (parent == null) {
            return this;
        }
        if (parent instanceof VBox) {
            VBox.setVgrow(fxNode, Priority.ALWAYS);
        }
        if (parent instanceof HBox) {
            HBox.setHgrow(fxNode, Priority.ALWAYS);
        }
        if (fxNode instanceof Region region) {
            region.setMaxHeight(Double.MAX_VALUE);
        }
        return this;
    }

    public Style alignment(Pos pos) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof VBox vBox) {
            vBox.setAlignment(pos);
        }
        if (fxNode instanceof HBox hBox) {
            hBox.setAlignment(pos);
        }
        return this;
    }

    public Style disable(boolean disable) {
        Node fxNode = this.baseComponent.getFxNode();
        fxNode.setDisable(disable);
        return this;
    }

    public Style display(boolean display) {
        Node fxNode = this.baseComponent.getFxNode();
        try {
            fxNode.setVisible(display);
        } catch (Throwable _) {}
        fxNode.setManaged(display);
        return this;
    }

    public Style setGap(int gap) {
        Node fxNode = this.baseComponent.getFxNode();
        if (fxNode instanceof VBox vBox) {
            vBox.setSpacing(gap);
        }
        if (fxNode instanceof HBox hBox) {
            hBox.setSpacing(gap);
        }
        if (fxNode instanceof GridPane gridPane) {
            gridPane.setHgap(gap);
        }
        return this;
    }

}