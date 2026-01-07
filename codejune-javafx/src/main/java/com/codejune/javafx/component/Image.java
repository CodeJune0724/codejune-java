package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.entity.PropertyType;
import com.codejune.javafx.entity.PropertyBind;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.InputStream;

public class Image extends BaseComponent {

    private final ImageView imageView = new ImageView();

    public Image(Object image) {
        this.setImage(image);
        this.imageView.setSmooth(true);
    }

    public Image() {
        this(null);
    }

    @Override
    public Node getFxNode() {
        return this.imageView;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == ImagePropertyType.IMAGE) {
            return () -> this.setImage(propertyBind.get());
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    public void autoSize() {
        BaseComponent parent = this.getParent();
        if (parent != null) {
            this.setWidth(parent.getStyle().getWidth());
        }
    }

    public void setWidth(int width) {
        this.imageView.setFitWidth(width);
        this.imageView.setPreserveRatio(true);
    }

    private void setImage(Object image) {
        switch (image) {
            case null -> this.imageView.setImage(null);
            case File file -> this.imageView.setImage(new javafx.scene.image.Image("file:///" + file.getAbsolutePath()));
            case InputStream inputStream -> this.imageView.setImage(new javafx.scene.image.Image(inputStream));
            default -> this.imageView.setImage(new javafx.scene.image.Image(ObjectUtil.toString(image)));
        }
    }

    public enum ImagePropertyType implements PropertyType {

        IMAGE

    }

}