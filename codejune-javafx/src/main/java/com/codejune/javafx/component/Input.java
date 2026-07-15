package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.bind.*;
import javafx.scene.Node;
import javafx.scene.control.TextField;

public final class Input extends BaseComponent {

    private final TextField textField = new TextField();

    public Input(String value) {
        this.setValue(value);
        this.getStyle().addSheet(getClass().getResource("/javafx/style/input.css")).addClass("input");
    }

    public Input() {
        this("");
    }

    @Override
    public Node getFxNode() {
        return this.textField;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == BasePropertyType.VALUE) {
            this.textField.textProperty().addListener((_, _, _) -> propertyBind.setObject(this.getValue()));
            return () -> this.setValue(ObjectUtil.toString(propertyBind.get()));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            this.textField.textProperty().addListener((_, _, _) -> asynchronousRun(runnable, asynchronous));
            return () -> {};
        }
        return super.customEventBind(eventType, runnable, asynchronous);
    }

    public void setPlaceholder(String placeholder) {
        this.textField.setPromptText(placeholder);
    }

    public String getValue() {
        return this.textField.getText();
    }

    public void setValue(String value) {
        int caretPosition = this.textField.getCaretPosition();
        this.textField.setText(value);
        int newPosition = Math.min(caretPosition, value == null ? 0 : value.length());
        this.textField.positionCaret(newPosition);
    }

}