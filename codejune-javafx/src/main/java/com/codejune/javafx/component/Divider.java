package com.codejune.javafx.component;

import com.codejune.core.util.StringUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;

public final class Divider extends BaseComponent {

    private final Grid grid = new Grid(1, 9);

    public Divider(String text) {
        this.grid.getStyle().addSheet("/style/divider.css").addClass("divider");
        this.grid.add(0, new Div(), div -> {
            div.getStyle().alignment(Pos.CENTER_LEFT);
            div.add(new Div(), divLine -> divLine.getStyle().addClass("divider-line"));
        });
        this.grid.add(1, new Div(Div.Layout.CELL), div -> {
            div.getStyle().alignment(Pos.CENTER_LEFT);

            div.add(new Text(text), textComponent -> {
                if (!StringUtil.isEmpty(text)) {
                    textComponent.getStyle().setMargin(0, 10, 0, 10);
                }
            });

            div.add(new Div(), divLineMain -> {
                divLineMain.getStyle().maxWidth().alignment(Pos.CENTER_LEFT);
                divLineMain.add(new Div(), divLine -> divLine.getStyle().addClass("divider-line"));
            });
        });
    }

    public Divider() {
        this(null);
    }

    @Override
    public Node getFxNode() {
        return this.grid.getFxNode();
    }

}