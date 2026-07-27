package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.bind.*;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseComponent {

    private BaseComponent parent = null;

    private final List<BaseComponent> childList = new ArrayList<>();

    private final Set<PropertyBind<?>> propertyBindList = new HashSet<>();

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

    public List<BaseComponent> getChildList() {
        return this.childList;
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
            this.childList.add(baseComponent);
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

    public void delete() {
        if (this.getFxNode() instanceof Parent parentItem) {
            deleteNode(parentItem);
        }
        if (this.parent != null) {
            Node parentNode = this.parent.getFxNode();
            if (parentNode instanceof Pane pane) {
                pane.getChildren().remove(this.getFxNode());
            }
            else if (parentNode instanceof Group group) {
                group.getChildren().remove(this.getFxNode());
            }
        }
        this.deleteChild();
        this.childList.clear();
        for (PropertyBind<?> propertyBind : this.propertyBindList) {
            propertyBind.deleteListener(this);
        }
    }

    public void deleteChild() {
        for (BaseComponent baseComponent : this.childList) {
            baseComponent.delete();
        }
    }

    /**
     * 绑定属性
     *
     * @param propertyType propertyType
     * @param propertyBind propertyBind
     * @param getValue 获取值
     * */
    public final void propertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<Object> getValue) {
        if (propertyType == null) {
            return;
        }
        if (propertyBind == null) {
            return;
        }
        if (getValue == null) {
            return;
        }
        Map<PropertyType, Runnable> bindMap = new HashMap<>();
        bindMap.put(BasePropertyType.DISABLE, () -> this.getStyle().disable(getValue.get() != null && ObjectUtil.parse(getValue.get(), boolean.class)));
        bindMap.put(BasePropertyType.DISPLAY, () -> this.getStyle().display(getValue.get() != null && ObjectUtil.parse(getValue.get(), boolean.class)));
        Runnable customBind = this.customPropertyBind(propertyType, propertyBind);
        if (customBind != null) {
            bindMap.put(propertyType, customBind);
        }
        Runnable runnable = bindMap.get(propertyType);
        if (runnable != null) {
            asynchronousRun(runnable, false, true);
            propertyBind.addListener(this, runnable);
            this.propertyBindList.add(propertyBind);
        }
    }

    /**
     * 绑定属性
     *
     * @param propertyType propertyType
     * @param propertyBind propertyBind
     * */
    public final void propertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyBind == null) {
            return;
        }
        this.propertyBind(propertyType, propertyBind, propertyBind::get);
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
        nestingEventMap.put(BaseEventType.CLICK, () -> this.getFxNode().setOnMouseClicked(_ -> asynchronousRun(runnable, asynchronous)));
        nestingEventMap.put(BaseEventType.MOUSE_MOVE, () -> this.getFxNode().setOnMouseMoved(_ -> asynchronousRun(runnable, asynchronous)));
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

    public static void asynchronousRun(Runnable runnable, boolean asynchronous, boolean runLater) {
        if (runnable == null) {
            return;
        }
        if (asynchronous) {
            if (runLater) {
                Thread.startVirtualThread(() -> Platform.runLater(runnable));
            } else {
                Thread.startVirtualThread(runnable);
            }
        } else {
            if (runLater) {
                Platform.runLater(runnable);
            } else {
                runnable.run();
            }
        }
    }

    public static void asynchronousRun(Runnable runnable, boolean asynchronous) {
        asynchronousRun(runnable, asynchronous, false);
    }

    private static void deleteNode(Parent parent) {
        List<Node> nodeChildren = new ArrayList<>(parent.getChildrenUnmodifiable());
        for (Node child : nodeChildren) {
            if (child instanceof Parent) {
                deleteNode((Parent) child);
            }
            if (parent instanceof Pane) {
                ((Pane) parent).getChildren().remove(child);
            }
            else if (parent instanceof Group) {
                ((Group) parent).getChildren().remove(child);
            }
        }
    }

}