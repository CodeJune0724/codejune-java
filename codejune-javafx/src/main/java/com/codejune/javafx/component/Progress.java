package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.bind.BasePropertyType;
import com.codejune.javafx.bind.PropertyBind;
import com.codejune.javafx.bind.PropertyType;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import java.util.function.Supplier;

public final class Progress extends BaseComponent {

    private final ProgressBar progressBar = new ProgressBar(0.5);

    public Progress() {
        this.getStyle().addSheet("/javafx/style/progress.css").addClass("progress");
    }

    @Override
    public Node getFxNode() {
        return this.progressBar;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        if (propertyType == BasePropertyType.VALUE) {
            return () -> this.setProgress(getValue.get() == null ? 0 : ObjectUtil.parse(getValue.get(), double.class));
        }
        return super.customPropertyBind(propertyType, propertyBind, getValue);
    }

    public void setProgress(double progress) {
        this.progressBar.setProgress(progress);
    }

}