package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.entity.*;
import javafx.scene.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Tab<KEY, ITEM> extends BaseComponent {

    private final Div main = new Div();

    private final Div tab = new Div(Div.Layout.CELL);

    private final Div body = new Div();

    private final Map<KEY, Button> tabButtonMap = new HashMap<>();

    private final Map<KEY, Div> bodyItemMap = new HashMap<>();

    private Function<ITEM, KEY> keyHandler = null;

    private Function<ITEM, String> titleRender = null;

    private Function<ITEM, BaseComponent> bodyRender = null;

    private final PropertyBind<KEY> value = new PropertyBind<>(null);

    public Tab() {
        this.main.getStyle().setGap(10);
        this.main.add(tab);
        this.main.add(body);
        this.value.addListener(() -> {
            for (KEY key : this.tabButtonMap.keySet()) {
                Button button = this.tabButtonMap.get(key);
                Div bodyDiv = this.bodyItemMap.get(key);
                if (ObjectUtil.equals(key, this.value.get())) {
                    button.getStyle().addCss("-fx-border-color: -fx-base-border-color -fx-base-border-color transparent -fx-base-border-color").setColor("-fx-base-primary-color");
                    bodyDiv.getStyle().display(true);
                } else {
                    button.getStyle().addCss("-fx-border-color: -fx-base-border-color -fx-base-border-color -fx-base-border-color -fx-base-border-color").setColor("rgba(0, 0, 0, 0.8)");
                    bodyDiv.getStyle().display(false);
                }
            }
        });
    }

    @Override
    public Node getFxNode() {
        return this.main.getFxNode();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == BasePropertyType.VALUE) {
            this.value.addListener(() -> propertyBind.setObject(this.value.get()));
            return () -> this.setValue((KEY) propertyBind.get());
        }
        if (propertyType == BasePropertyType.DATA) {
            return () -> this.setData((List<ITEM>) propertyBind.get());
        }
        return super.customPropertyBind(propertyType, propertyBind);
    }

    @Override
    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == BaseEventType.VALUE) {
            return () -> this.value.addListener(() -> asynchronousRun(asynchronous, runnable));
        }
        return super.customEventBind(eventType, runnable, asynchronous);
    }

    public void setValue(KEY key) {
        this.value.set(key);
    }

    @SuppressWarnings("unchecked")
    public void setData(List<ITEM> list) {
        this.tab.deleteChild();
        this.body.deleteChild();
        this.tabButtonMap.clear();
        this.bodyItemMap.clear();
        if (list == null) {
            return;
        }
        for (ITEM item : list) {
            KEY key = this.keyHandler == null ? (KEY) item : this.keyHandler.apply(item);
            this.tab.add(new Button(this.titleRender != null ? this.titleRender.apply(item) : ObjectUtil.toString(item)), button -> {
                button.getStyle().setFontWeight("none").addCss("-fx-border-radius: 5px 5px 0 0");
                button.eventBind(BaseEventType.CLICK, () -> this.setValue(key));
                this.tabButtonMap.put(key, button);
            });
            this.body.add(new Div(), div -> {
                div.getStyle().display(false);
                div.add(this.bodyRender == null ? null : this.bodyRender.apply(item));
                this.bodyItemMap.put(key, div);
            });
        }
    }

    public void setKeyHandler(Function<ITEM, KEY> keyHandler) {
        this.keyHandler = keyHandler;
    }

    public void setTitleRender(Function<ITEM, String> titleRender) {
        this.titleRender = titleRender;
    }

    public void setBodyRender(Function<ITEM, BaseComponent> bodyRender) {
        this.bodyRender = bodyRender;
    }

}