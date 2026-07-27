package com.codejune.javafx.bind;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.component.BaseComponent;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PropertyBind<T> {

    private final AtomicReference<T> data = new AtomicReference<>();

    private final Map<Object, List<Runnable>> listener = new LinkedHashMap<>();

    private Map<Object, List<Runnable>> listenerCache = null;

    public PropertyBind(T data) {
        this.set(data);
    }

    public PropertyBind() {
        this.data.set(null);
    }

    public final T get() {
        return this.data.get();
    }

    public synchronized final void set(T data) {
        if (ObjectUtil.equals(this.get(), data) && !(data instanceof Collection<?>)) {
            return;
        }
        this.data.set(data);
        try {
            this.listenerCache = new LinkedHashMap<>();
            this.listener.values().forEach((runnableList) -> {
                for (Runnable runnable : runnableList) {
                    BaseComponent.asynchronousRun(runnable, false, true);
                }
            });
            for (Map.Entry<Object, List<Runnable>> entry : this.listenerCache.entrySet()) {
                for (Runnable runnable : entry.getValue()) {
                    BaseComponent.asynchronousRun(runnable, false, true);
                }
                List<Runnable> runnableList = this.listener.get(entry.getKey());
                if (runnableList == null) {
                    runnableList = new ArrayList<>();
                }
                runnableList.addAll(entry.getValue());
                this.listener.put(entry.getKey(), runnableList);
            }
        } finally {
            this.listenerCache = null;
        }
    }

    @SuppressWarnings("unchecked")
    public final void setObject(Object data) {
        this.set(ObjectUtil.parse(data, data == null ? null : (Class<? extends T>) data.getClass()));
    }

    public final void addListener(Object id, Runnable runnable) {
        if (id == null) {
            id = "DEFAULT";
        }
        if (runnable == null) {
            return;
        }
        Map<Object, List<Runnable>> addListener;
        if (!ObjectUtil.isEmpty(this.listenerCache)) {
            addListener = listenerCache;
        } else {
            addListener = this.listener;
        }
        List<Runnable> runnableList = addListener.get(id);
        if (runnableList == null) {
            runnableList = new ArrayList<>();
        }
        runnableList.add(runnable);
        this.listener.put(id, runnableList);
    }

    public final void addListener(Runnable runnable) {
        this.addListener(null, runnable);
    }

    /**
     * 删除监听
     *
     * @param id id
     * */
    public final void deleteListener(Object id) {
        this.listener.remove(id);
    }

}