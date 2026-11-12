package com.codejune.javafx.component;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.Application;
import com.codejune.javafx.Window;
import com.codejune.javafx.bind.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseComponent {

    // window
    private Window window;

    // 父级元素
    private BaseComponent parent = null;

    // 子元素
    private final List<BaseComponent> childList = new ArrayList<>();

    // 所有绑定的数据
    private final Set<PropertyBind<?>> propertyBindList = new HashSet<>();

    // style
    private final Style style = new Style(this);

    // 所有事件
    private final Map<EventType, Runnable> eventMap = new HashMap<>();

    // 删除组件后，需要一并删除的回调
    private final List<Runnable> deleteActionList = new ArrayList<>();

    // 组件是否已经删除
    private volatile boolean deleted;

    /**
     * getFxNode
     *
     * @return Node
     * */
    public abstract Node getFxNode();

    /**
     * getAddFxNode
     *
     * @return Node
     * */
    protected Node getAddFxNode() {
        return this.getFxNode();
    }

    /**
     * 自定义属性绑定
     *
     * @param propertyType propertyType
     * @param propertyBind propertyBind
     * @param getValue getValue
     *
     * @return Runnable
     * */
    protected Runnable customPropertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        return null;
    }

    protected Runnable customEventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        return null;
    }

    /**
     * 是否为数据刷新的属性
     *
     * @param propertyType propertyType
     *
     * @return boolean
     * */
    protected boolean coalesceProperty(PropertyType propertyType) {
        return false;
    }

    /**
     * 组件删除的调用
     * */
    protected void onDelete() {}

    /**
     * isDeleted
     *
     * @return boolean
     * */
    public final boolean isDeleted() {
        return this.deleted;
    }

    /**
     * getWindow
     *
     * @return Window
     * */
    public final Window getWindow() {
        return this.window;
    }

    /**
     * setWindow
     *
     * @param window window
     * */
    public final void setWindow(Window window) {
        this.window = window;
        this.childList.forEach(child -> child.setWindow(window));
    }

    /**
     * getParent
     *
     * @return BaseComponent
     * */
    public final BaseComponent getParent() {
        return this.parent;
    }

    /**
     * setParent
     *
     * @param baseComponent baseComponent
     * */
    protected final void setParent(BaseComponent baseComponent) {
        this.parent = baseComponent;
    }

    /**
     * getChildList
     *
     * @return List<BaseComponent>
     * */
    public final List<BaseComponent> getChildList() {
        return this.childList;
    }

    /**
     * 添加组件
     *
     * @param baseComponent baseComponent
     * @param action 回调
     *
     * @return T
     * */
    public final <T extends BaseComponent> T add(T baseComponent, Consumer<T> action) {
        if (baseComponent == null) {
            return null;
        }
        this.checkActive();
        ((BaseComponent) baseComponent).checkActive();
        if (action == null) {
            action = _ -> {};
        }
        Node fxNode = this.getAddFxNode();
        if (fxNode instanceof Pane pane) {
            pane.getChildren().add(baseComponent.getFxNode());
            this.registerChild(baseComponent);
            action.accept(baseComponent);
        }
        return baseComponent;
    }

    public final <T extends BaseComponent> T add(T baseComponent) {
        return this.add(baseComponent, null);
    }

    /**
     * getStyle
     *
     * @return Style
     * */
    public final Style getStyle() {
        return this.style;
    }

    /**
     * 删除当前组件
     * */
    public final void delete() {
        if (this.deleted) {
            return;
        }
        this.deleted = true;
        for (PropertyBind<?> propertyBind : this.propertyBindList) {
            propertyBind.deleteListener(this);
        }
        this.propertyBindList.clear();
        this.deleteActionList.forEach(Runnable::run);
        this.deleteActionList.clear();
        this.onDelete();
        this.deleteChild();
        if (this.parent != null) {
            Node parentNode = this.parent.getAddFxNode();
            if (parentNode instanceof Pane pane) {
                pane.getChildren().remove(this.getFxNode());
            }
            else if (parentNode instanceof Group group) {
                group.getChildren().remove(this.getFxNode());
            }
        }
        if (this.parent != null) {
            this.parent.childList.remove(this);
            this.parent = null;
        }
        this.window = null;
        this.eventMap.clear();
        this.getFxNode().setOnMouseClicked(null);
        this.getFxNode().setOnMouseMoved(null);
    }

    /**
     * 删除所有子组件
     * */
    public final void deleteChild() {
        for (BaseComponent baseComponent : new ArrayList<>(this.childList)) {
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
    public final void propertyBind(PropertyType propertyType, PropertyBind<?> propertyBind, Supplier<?> getValue) {
        if (propertyType == null) {
            return;
        }
        if (propertyBind == null) {
            return;
        }
        this.checkActive();
        Map<PropertyType, Runnable> bindMap = new HashMap<>();
        bindMap.put(BasePropertyType.DISABLE, () -> {
            Object propertyBindValue = getPropertyBindValue(propertyBind, getValue);
            this.getStyle().disable(propertyBindValue != null && ObjectUtil.parse(propertyBindValue, boolean.class));
        });
        bindMap.put(BasePropertyType.DISPLAY, () -> {
            Object propertyBindValue = getPropertyBindValue(propertyBind, getValue);
            this.getStyle().display(propertyBindValue != null && ObjectUtil.parse(propertyBindValue, boolean.class));
        });
        Runnable customBind = this.customPropertyBind(propertyType, propertyBind, getValue);
        if (customBind != null) {
            bindMap.put(propertyType, customBind);
        }
        Runnable runnable = bindMap.get(propertyType);
        if (runnable != null) {
            this.propertyBindList.add(propertyBind);
            propertyBind.addListener(this, () -> {
                if (!this.deleted) runnable.run();
            }, this.coalesceProperty(propertyType), true);
        }
    }

    public final void propertyBind(PropertyType propertyType, PropertyBind<?> propertyBind) {
        if (propertyBind == null) {
            return;
        }
        this.propertyBind(propertyType, propertyBind, null);
    }

    /**
     * 绑定事件
     *
     * @param eventType eventType
     * @param runnable runnable
     * @param asynchronous asynchronous
     * */
    public final void eventBind(EventType eventType, Runnable runnable, boolean asynchronous) {
        if (eventType == null) {
            return;
        }
        if (runnable == null) {
            return;
        }
        this.checkActive();
        Runnable guarded = () -> {
            if (!this.deleted) runnable.run();
        };
        this.eventMap.put(eventType, guarded);
        Map<EventType, Runnable> nestingEventMap = new HashMap<>();
        nestingEventMap.put(BaseEventType.CLICK, () -> this.getFxNode().setOnMouseClicked(_ -> Application.asynchronousRun(guarded, asynchronous)));
        nestingEventMap.put(BaseEventType.MOUSE_MOVE, () -> this.getFxNode().setOnMouseMoved(_ -> Application.asynchronousRun(guarded, asynchronous)));
        Runnable customAction = this.customEventBind(eventType, guarded, asynchronous);
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

    /**
     * 监听绑定
     *
     * @param propertyBind propertyBind
     * @param runnable runnable
     * */
    public final void listenerBind(PropertyBind<?> propertyBind, Runnable runnable) {
        if (propertyBind == null || runnable == null) {
            return;
        }
        this.checkActive();
        this.propertyBindList.add(propertyBind);
        propertyBind.addListener(this, () -> {
            if (!this.deleted) runnable.run();
        });
    }

    /**
     * 直接调用某个事件
     *
     * @param eventType eventType
     * */
    public final void action(EventType eventType) {
        Runnable runnable = this.eventMap.get(eventType);
        if (runnable != null) {
            runnable.run();
        }
    }

    /**
     * tooltip
     *
     * @param text text
     * */
    public final void tooltip(String text) {
        if (this.getFxNode() instanceof Control control) {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(javafx.util.Duration.millis(100));
            tooltip.setWrapText(true);
            control.setTooltip(tooltip);
        }
    }

    /**
     * 原生组件添加监听，方便清空
     *
     * @param value value
     * @param listener listener
     * */
    protected final <T> void observe(ObservableValue<T> value, ChangeListener<? super T> listener) {
        this.checkActive();
        ChangeListener<T> guarded = (observable, oldValue, newValue) -> {
            if (!this.deleted) listener.changed(observable, oldValue, newValue);
        };
        value.addListener(guarded);
        this.deleteActionList.add(() -> value.removeListener(guarded));
    }

    /**
     * 注册其他非直接 Pane 子节点的组件
     *
     * @param baseComponent baseComponent
     * */
    protected final void registerChild(BaseComponent baseComponent) {
        this.checkActive();
        baseComponent.checkActive();
        if (baseComponent.parent != null) baseComponent.parent.childList.remove(baseComponent);
        baseComponent.setParent(this);
        baseComponent.setWindow(this.getWindow());
        this.childList.add(baseComponent);
    }

    /**
     * 检查状态
     * */
    private void checkActive() {
        if (this.deleted) throw new IllegalStateException("Component has been deleted");
    }

    /**
     * 属性绑定后，用于获取实际值
     *
     * @param propertyBind propertyBind
     * @param getValue getValue
     *
     * @return Object
     * */
    public static Object getPropertyBindValue(PropertyBind<?> propertyBind, Supplier<?> getValue) {
        Object result;
        if (getValue != null) {
            result = getValue.get();
        } else {
            result = propertyBind.get();
        }
        return result;
    }

}