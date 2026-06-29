package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.javafx.entity.BasePropertyType;
import com.codejune.javafx.entity.PropertyBind;
import com.codejune.javafx.entity.PropertyType;
import javafx.scene.Node;

public final class Card extends BaseComponent {

    private final Div div = new Div();

    private final PropertyBind<String> title = new PropertyBind<>(null);

    public Card() {
        this.div.getStyle().setPadding(10, 10, 10, 10);

        this.getStyle().addSheet("/javafx/style/card.css");

        this.add(new Text(), text -> {
            text.getStyle().setMargin(0, 0, 10, 0).display(false).setFontSize(14).setFontWeight("bold");
            text.propertyBind(BasePropertyType.TEXT, this.title);
            text.propertyBind(BasePropertyType.DISPLAY, this.title.parseBind(s -> !StringUtil.isEmpty(s)));
        });

        this.setType(Type.DEFAULT);
        this.setTitle(null);
    }

    @Override
    public Node getFxNode() {
        return this.div.getFxNode();
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == CardPropertyType.TITLE) {
            return () -> this.setTitle(ObjectUtil.toString(propertyBind.get()));
        }
        if (propertyType == CardPropertyType.TYPE) {
            return () -> this.setType(ObjectUtil.parse(propertyBind.get(), Type.class));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void hover(boolean hover) {
        if (hover) {
            this.getStyle().addClass("cardHover");
        } else {
            this.getStyle().deleteClass("cardHover");
        }
    }

    public void setType(Type type) {
        this.getStyle()
                .addCss("-fx-background-color: " + type.backgroundColor + ";")
                .addCss("-fx-border-color: " + type.borderColor + ";")
                .addCss("-fx-border-width: 1;")
                .addCss("-fx-border-radius: 5;")
                .addCss("-fx-background-radius: 5;");
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public enum Type {

        DEFAULT("white", "#e0e0e0"),

        PRIMARY("rgb(230, 244, 255)", "rgb(145, 202, 255)"),

        SUCCESS("rgb(246, 255, 237)", "rgb(183, 235, 143)"),

        INFO("#F4F4F5", "-fx-base-border-color"),

        WARN("rgb(255, 251, 230)", "rgb(255, 229, 143)"),

        ERROR("rgb(255, 242, 240)", "rgb(255, 204, 199)");

        private final String backgroundColor;

        private final String borderColor;

        Type(String backgroundColor, String borderColor) {
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
        }

    }

    public enum CardPropertyType implements PropertyType {

        TITLE,

        TYPE

    }

}