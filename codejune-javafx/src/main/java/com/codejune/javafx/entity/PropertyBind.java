package com.codejune.javafx.entity;

import com.codejune.core.util.ObjectUtil;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class PropertyBind<T> {

    private final AtomicReference<T> data = new AtomicReference<>();

    private final List<Runnable> listenerList = new ArrayList<>();

    public PropertyBind(T data) {
        this.set(data);
    }

    public PropertyBind() {
        this.data.set(null);
    }

    public final T get() {
        return this.data.get();
    }

    public final void set(T data) {
        if (ObjectUtil.equals(this.get(), data) && !(data instanceof Collection<?>)) {
            return;
        }
        this.data.set(data);
        for (Runnable runnable : this.listenerList) {
            runnable.run();
        }
    }

    @SuppressWarnings("unchecked")
    public final void setObject(Object data) {
        this.set(ObjectUtil.parse(data, data == null ? null : (Class<? extends T>) data.getClass()));
    }

    public final void addListener(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        this.listenerList.add(() -> Platform.runLater(runnable));
    }

    public final <R> PropertyBind<R> parseBind(Function<T, R> function) {
        if (function == null) {
            function = _ -> null;
        }
        Function<T, R> finalFunction = function;
        PropertyBind<R> result = new PropertyBind<>(function.apply(this.get()));
        this.addListener(() -> result.set(finalFunction.apply(this.get())));
        return result;
    }

}