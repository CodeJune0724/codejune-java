package com.codejune.javafx.entity;

import com.codejune.core.util.ObjectUtil;
import javafx.application.Platform;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class PropertyBind<T> {

    private final AtomicReference<T> data = new AtomicReference<>();

    private final Map<String, Runnable> listener = new LinkedHashMap<>();

    private Map<String, Runnable> listenerCache = null;

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
            this.listener.values().forEach(Runnable::run);
            for (Map.Entry<String, Runnable> entry : this.listenerCache.entrySet()) {
                entry.getValue().run();
                this.listener.put(entry.getKey(), entry.getValue());
            }
        } finally {
            this.listenerCache = null;
        }
    }

    @SuppressWarnings("unchecked")
    public final void setObject(Object data) {
        this.set(ObjectUtil.parse(data, data == null ? null : (Class<? extends T>) data.getClass()));
    }

    public final void addListener(Runnable runnable, String id) {
        if (runnable == null) {
            return;
        }
        Objects.requireNonNullElse(this.listenerCache, this.listener).put(id, () -> Platform.runLater(runnable));
    }

    public final void addListener(Runnable runnable) {
        this.addListener(runnable, UUID.randomUUID().toString());
    }

    public final <R> PropertyBind<R> parseBind(Function<T, R> function, String id) {
        if (function == null) {
            function = _ -> null;
        }
        Function<T, R> finalFunction = function;
        PropertyBind<R> result = new PropertyBind<>(function.apply(this.get()));
        this.addListener(() -> result.set(finalFunction.apply(this.get())), id);
        return result;
    }

    public final <R> PropertyBind<R> parseBind(Function<T, R> function) {
        return this.parseBind(function, UUID.randomUUID().toString());
    }

}