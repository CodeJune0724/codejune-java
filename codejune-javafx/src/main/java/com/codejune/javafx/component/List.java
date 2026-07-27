package com.codejune.javafx.component;

import com.codejune.javafx.bind.BasePropertyType;
import com.codejune.javafx.bind.PropertyBind;
import com.codejune.javafx.bind.PropertyType;
import javafx.scene.Node;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class List<T> extends BaseComponent {

    private final Div div = new Div();

    private BiConsumer<Integer, T> render;

    @Override
    public Node getFxNode() {
        return this.div.getFxNode();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        if (propertyType == BasePropertyType.DATA) {
            return () -> this.init((java.util.List<T>) getValue.get());
        }
        return super.customPropertyBind(propertyType, propertyBind, getValue);
    }

    private synchronized void init(java.util.List<T> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        this.deleteChild();
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            this.render.accept(i, t);
        }
    }

    public void setRender(BiConsumer<Integer, T> render) {
        this.render = render;
    }

}