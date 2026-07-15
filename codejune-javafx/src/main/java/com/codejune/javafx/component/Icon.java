package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.bind.BasePropertyType;
import com.codejune.javafx.bind.PropertyBind;
import com.codejune.javafx.bind.PropertyType;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

public final class Icon extends BaseComponent {

    private final FontIcon fontIcon = new FontIcon();

    public Icon(Ikon ikon) {
        this.setIcon(ikon);
    }

    public Icon() {}

    @Override
    public Node getFxNode() {
        return this.fontIcon;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == BasePropertyType.COLOR) {
            return () -> this.setColor(ObjectUtil.parse(propertyBind.get(), String.class));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void setIcon(Ikon ikon) {
        this.fontIcon.setIconCode(ikon);
    }

    public void setSize(int size) {
        this.fontIcon.setIconSize(size);
    }

    public void setColor(String color) {
        this.fontIcon.setIconColor(Paint.valueOf(color));
    }

}