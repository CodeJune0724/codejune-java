package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.bind.BasePropertyType;
import com.codejune.javafx.bind.PropertyBind;
import com.codejune.javafx.bind.PropertyType;
import javafx.scene.Node;

public final class Button extends BaseComponent {

    private final javafx.scene.control.Button button = new javafx.scene.control.Button();

    public Button(String text) {
        this.setText(text);
        this.getStyle().addSheet(getClass().getResource("/javafx/style/button.css"));
        this.getStyle().addClass("button");
        this.setType(Type.DEFAULT);
        this.setSize(Size.DEFAULT);
    }

    @Override
    public Node getFxNode() {
        return this.button;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == ButtonPropertyType.TYPE) {
            return () -> this.setType(ObjectUtil.parse(propertyBind.get(), Type.class));
        }
        if (propertyType == BasePropertyType.TEXT) {
            return () -> this.setText(ObjectUtil.parse(propertyBind.get(), String.class));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void setSize(Size size) {
        if (size == null) {
            return;
        }
        for (Size item : Size.values()) {
            this.getStyle().deleteClass(item.aClass);
        }
        this.button.getStyleClass().add(size.aClass);
    }

    public void setType(Type type) {
        if (type == null) {
            return;
        }
        for (Type item : Type.values()) {
            this.getStyle().deleteClass(item.aClass);
        }
        this.getStyle().addClass(type.aClass);
    }

    public void setText(String text) {
        this.button.setText(text);
    }

    public enum Type {

        DEFAULT("button-default"),

        PRIMARY("button-primary"),

        SUCCESS("button-success"),

        WARN("button-warn"),

        ERROR("button-error"),

        PRIMARY_PLAIN("button-primary-plain"),

        SUCCESS_PLAIN("button-success-plain"),

        WARN_PLAIN("button-warn-plain"),

        ERROR_PLAIN("button-error-plain");

        private final String aClass;

        Type(String aClass) {
            this.aClass = aClass;
        }

    }

    public enum Size {

        LARGE("button-size-large"),

        DEFAULT("button-size-default"),

        SMALL("button-size-small");

        private final String aClass;

        Size(String aClass) {
            this.aClass = aClass;
        }

    }

    public enum ButtonPropertyType implements PropertyType {

        TYPE

    }

}