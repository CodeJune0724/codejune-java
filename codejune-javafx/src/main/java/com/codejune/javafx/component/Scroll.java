package com.codejune.javafx.component;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

public final class Scroll extends BaseComponent {

    private final ScrollPane scrollPane = new ScrollPane();

    private final Div div = new Div();

    public Scroll() {
        this.getStyle().addSheet("/javafx/style/scroll.css").addClass("scroll");
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setFitToHeight(true);
        this.scrollPane.setContent(this.div.getFxNode());
    }

    @Override
    public Node getFxNode() {
        return this.scrollPane;
    }

    @Override
    protected Node getAddFxNode() {
        return this.div.getFxNode();
    }

}