package com.codejune.javafx.component;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import java.util.function.Consumer;

public final class Grid extends BaseComponent {

    private final GridPane gridPane = new GridPane();

    public Grid(double... colum) {
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.ALWAYS);
        this.gridPane.getRowConstraints().add(rowConstraints);
        for (double item : colum) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(item * 100);
            this.gridPane.getColumnConstraints().add(columnConstraints);
        }
    }

    @Override
    public Node getFxNode() {
        return this.gridPane;
    }

    public <T extends BaseComponent> void add(int columIndex, T baseComponent, Consumer<T> action) {
        if (baseComponent == null) {
            return;
        }
        if (action == null) {
            action = _ -> {};
        }
        baseComponent.setParent(this);
        action.accept(baseComponent);
        GridPane.setValignment(baseComponent.getFxNode(), VPos.CENTER);
        this.gridPane.add(baseComponent.getFxNode(), columIndex, 0);
    }

    public <T extends BaseComponent> void add(int columIndex, T baseComponent) {
        this.add(columIndex, baseComponent, null);
    }

}