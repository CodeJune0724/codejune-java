package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.entity.BasePropertyType;
import com.codejune.javafx.entity.PropertyBind;
import com.codejune.javafx.entity.PropertyType;
import javafx.scene.Node;
import javafx.scene.control.Label;

public final class Text extends BaseComponent {

    private final Label label = new Label();

    public Text(String text) {
        this.setText(text);
        this.wrap(true);
    }

    public Text() {
        this(null);
    }

    @Override
    public Node getFxNode() {
        return this.label;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == BasePropertyType.TEXT) {
            return () -> this.setText(ObjectUtil.toString(propertyBind.get()));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void setColor(int red, int green, int blue) {
        this.getStyle().addCss("-fx-text-fill: rgb(" + red + "," + green + "," + blue + ");");
    }

    public void setText(String text) {
        this.label.setText(text);
    }

    public void wrap(boolean wrap) {
        this.label.setWrapText(wrap);
    }

}