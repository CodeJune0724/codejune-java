package com.codejune.javafx.component;

import com.codejune.core.util.StringUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;

public final class Divider extends BaseComponent {

    private final Div div = new Div(Div.Layout.CELL);

    private final Text textComponent = new Text();

    public Divider(String text) {
        this.div.getStyle().addSheet("/javafx/style/divider.css").addClass("divider");
        this.add(new Div(), div -> {
            div.getStyle().alignment(Pos.CENTER_LEFT).setWidth(0.1);
            div.add(new Div(), divLine -> divLine.getStyle().addClass("divider-line"));
        });
        this.add(new Div(Div.Layout.CELL), div -> {
            div.getStyle().alignment(Pos.CENTER_LEFT).setWidth(0.9);

            div.add(this.textComponent, textComponent -> {
                textComponent.setText(text);
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

    @Override
    public Node getFxNode() {
        return this.div.getFxNode();
    }

    public Text getTextComponent() {
        return this.textComponent;
    }

}