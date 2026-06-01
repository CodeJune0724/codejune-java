package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.entity.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BaseComponent {

    private BaseComponent parent = null;

    private final Style style = new Style(this);

    private final Map<EventType, Runnable> eventMap = new HashMap<>();

    public abstract Node getFxNode();

    protected Node getAddFxNode() {
        return this.getFxNode();
    }

    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        return null;
    }

    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        return null;
    }

    public final BaseComponent getParent() {
        return this.parent;
    }

    protected final void setParent(BaseComponent baseComponent) {
        this.parent = baseComponent;
    }

    public final <T extends BaseComponent> T add(T baseComponent, Consumer<T> action) {
        if (baseComponent == null) {
            return null;
        }
        if (action == null) {
            action = _ -> {};
        }
        Node fxNode = this.getAddFxNode();
        if (fxNode instanceof Pane pane) {
            pane.getChildren().add(baseComponent.getFxNode());
            baseComponent.setParent(this);
            action.accept(baseComponent);
        }
        return baseComponent;
    }

    public final <T extends BaseComponent> T add(T baseComponent) {
        return this.add(baseComponent, null);
    }

    public final Style getStyle() {
        return this.style;
    }

    public final void deleteChild() {
        if (this.getFxNode() instanceof Pane pane) {
            pane.getChildren().removeAll(pane.getChildren());
        }
    }

    public final void deleteChild(BaseComponent baseComponent) {
        if (this.getFxNode() instanceof Pane pane) {
            pane.getChildren().remove(baseComponent.getFxNode());
        }
    }

    public final void propertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyType == null) {
            return;
        }
        if (propertyBind == null) {
            return;
        }
        Map<PropertyType, Runnable> bindMap = new HashMap<>();
        bindMap.put(BasePropertyType.DISABLE, () -> this.getStyle().disable(propertyBind.get() != null && ObjectUtil.parse(propertyBind.get(), boolean.class)));
        bindMap.put(BasePropertyType.DISPLAY, () -> this.getStyle().display(propertyBind.get() != null && ObjectUtil.parse(propertyBind.get(), boolean.class)));
        Runnable customBind = this.customPropertyBind(propertyType, propertyBind);
        if (customBind != null) {
            bindMap.put(propertyType, customBind);
        }
        Runnable runnable = bindMap.get(propertyType);
        if (runnable != null) {
            Platform.runLater(runnable);
            propertyBind.addListener(runnable);
        }
    }

    public final void eventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == null) {
            return;
        }
        if (runnable == null) {
            return;
        }
        this.eventMap.put(eventType, runnable);
        Map<EventType, Runnable> nestingEventMap = new HashMap<>();
        nestingEventMap.put(BaseEventType.CLICK, () -> this.getFxNode().setOnMouseClicked(_ -> asynchronousRun(asynchronous, runnable)));
        nestingEventMap.put(BaseEventType.MOUSE_MOVE, () -> this.getFxNode().setOnMouseMoved(_ -> asynchronousRun(asynchronous, runnable)));
        Runnable customAction = this.customEventBind(eventType, runnable, asynchronous);
        if (customAction != null) {
            nestingEventMap.put(eventType, customAction);
        }
        Runnable nestingActionRunnable = nestingEventMap.get(eventType);
        if (nestingActionRunnable != null) {
            nestingActionRunnable.run();
        }
    }

    public final void eventBind(EventType eventType, Runnable runnable) {
        this.eventBind(eventType, runnable, false);
    }

    public final void action(EventType eventType) {
        Runnable runnable = this.eventMap.get(eventType);
        if (runnable != null) {
            runnable.run();
        }
    }

    public final void tooltip(String text) {
        if (this.getFxNode() instanceof Control control) {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(javafx.util.Duration.millis(100));
            tooltip.setWrapText(true);
            control.setTooltip(tooltip);
        }
    }

    public static void asynchronousRun(boolean asynchronous, Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (asynchronous) {
            Thread.startVirtualThread(runnable);
        } else {
            runnable.run();
        }
    }

}