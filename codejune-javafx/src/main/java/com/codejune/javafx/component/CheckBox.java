package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.javafx.bind.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import java.util.function.Supplier;

public final class CheckBox extends BaseComponent {

    private final Label label = new Label();

    private final PropertyBind<Boolean> value = new PropertyBind<>(false);

    private final Icon icon = new Icon();

    public CheckBox(String text) {
        this.getStyle().addSheet("/javafx/style/checkBox.css").addClass("checkBox");
        Div div = new Div(Div.Layout.CELL);
        div.getStyle().alignment(Pos.CENTER_LEFT);
        div.add(icon, _ -> this.initIcon());
        if (!StringUtil.isEmpty(text)) {
            div.add(new Text(text), textComponent -> {
                textComponent.getStyle().setMargin(0, 0, 0, 5);
                textComponent.setText(text);
            });
        }
        this.label.setGraphic(div.getFxNode());
        this.eventBind(BaseEventType.CLICK, () -> {
            this.value.set(!this.value.get());
            this.initIcon();
        });
    }
    public CheckBox() {
        this(null);
    }

    @Override
    public Node getFxNode() {
        return this.label;
    }

    @Override
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        if (propertyType == BasePropertyType.VALUE) {
            this.value.addListener(() -> propertyBind.setObject(this.value.get()));
            return () -> this.setValue(ObjectUtil.parse(getValue.get(), Boolean.class));
        }
        return super.customPropertyBind(propertyType, propertyBind, getValue);
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            return () -> this.value.addListener(() -> asynchronousRun(runnable, asynchronous));
        }
        return super.customEventBind(eventType, runnable, asynchronous);
    }

    public void setValue(Boolean value) {
        if (value == null) {
            return;
        }
        this.value.set(value);
        this.initIcon();
    }

    public Boolean getValue() {
        return this.value.get();
    }

    private void initIcon() {
        if (this.value.get()) {
            this.icon.getStyle().setMargin(2, 0, 0, 0);
            this.icon.setSize(21);
            this.icon.setColor("#1677ff");
            this.icon.setIcon(FontAwesome.CHECK_SQUARE);
        }
        if (!this.value.get()) {
            this.icon.getStyle().setMargin(1, 0, 0, 0);
            this.icon.setSize(22);
            this.icon.setColor("rgb(217, 217, 217)");
            this.icon.setIcon(FontAwesome.SQUARE_O);
        }
    }

}