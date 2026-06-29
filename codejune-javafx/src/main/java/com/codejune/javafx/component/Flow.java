package com.codejune.javafx.component;

import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

public class Flow extends BaseComponent {

    private final FlowPane flowPane = new FlowPane();

    public Flow() {}

    @Override
    public Node getFxNode() {
        return this.flowPane;
    }

    public void setGap(int gap) {
        this.flowPane.setHgap(gap);
        this.flowPane.setVgap(gap);
    }

}