package com.codejune.javafx.component;

import com.codejune.core.BaseException;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public final class Div extends BaseComponent {

    private final Pane fxNode;

    public Div(Layout layout) {
        if (layout == Layout.ROW) {
            this.fxNode = new VBox();
        }
        else if (layout == Layout.CELL) {
            this.fxNode = new HBox();
        } else {
            throw new BaseException("layout error");
        }
    }

    public Div() {
        this(Layout.ROW);
    }

    @Override
    public Node getFxNode() {
        return this.fxNode;
    }

    public enum Layout {

        ROW,

        CELL

    }

}