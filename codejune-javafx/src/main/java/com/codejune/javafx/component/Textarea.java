package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.entity.BasePropertyType;
import com.codejune.javafx.entity.PropertyBind;
import com.codejune.javafx.entity.PropertyType;
import javafx.scene.Node;
import javafx.scene.control.TextArea;

public final class Textarea extends BaseComponent {

    private final TextArea textArea = new TextArea();

    public Textarea(int row) {
        this.textArea.setPrefRowCount(row);
        this.textArea.setWrapText(true);
        this.getStyle().addSheet(getClass().getResource("/style/textarea.css")).addClass("textarea");
    }

    @Override
    public Node getFxNode() {
        return this.textArea;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == BasePropertyType.VALUE) {
            this.textArea.textProperty().addListener((_, _, _) -> propertyBind.setObject(this.getValue()));
            return () -> this.setValue(ObjectUtil.toString(propertyBind.get()));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void setPlaceholder(String placeholder) {
        this.textArea.setPromptText(placeholder);
    }

    public String getValue() {
        return this.textArea.getText();
    }

    public void setValue(String value) {
        int caretPosition = this.textArea.getCaretPosition();
        this.textArea.setText(value);
        int newPosition = Math.min(caretPosition, value == null ? 0 : value.length());
        this.textArea.positionCaret(newPosition);
    }

}