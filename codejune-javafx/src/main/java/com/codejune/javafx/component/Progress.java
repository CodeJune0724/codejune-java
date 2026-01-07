package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.entity.BasePropertyType;
import com.codejune.javafx.entity.PropertyBind;
import com.codejune.javafx.entity.PropertyType;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;

public final class Progress extends BaseComponent {

    private final ProgressBar progressBar = new ProgressBar(0.5);

    public Progress() {
        this.getStyle().addSheet("/style/progress.css").addClass("progress");
    }

    @Override
    public Node getFxNode() {
        return this.progressBar;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == BasePropertyType.VALUE) {
            return () -> this.setProgress(propertyBind.get() == null ? 0 : ObjectUtil.parse(propertyBind.get(), double.class));
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void setProgress(double progress) {
        this.progressBar.setProgress(progress);
    }

}