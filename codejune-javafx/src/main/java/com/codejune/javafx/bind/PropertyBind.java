package com.codejune.javafx.bind;

import com.codejune.core.util.ObjectUtil;
import com.codejune.javafx.Application;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PropertyBind
 *
 * @param <T> T
 *
 * @author ZJ
 * */
public class PropertyBind<T> {

    // 数据
    private final AtomicReference<T> data = new AtomicReference<>();

    // 监听
    private final Map<Object, List<Listener>> listener = new LinkedHashMap<>();

    public PropertyBind(T data) {
        this.set(data);
    }

    public PropertyBind() {
        this.data.set(null);
    }

    /**
     * 获取数据
     *
     * @return T
     * */
    public final T get() {
        return this.data.get();
    }

    /**
     * 设置数据
     *
     * @param data data
     * */
    public final void set(T data) {
        List<Listener> snapshot;
        synchronized (this) {
            if (ObjectUtil.equals(this.get(), data) && !(data instanceof Collection<?>)) {
                return;
            }
            this.data.set(data);
            snapshot = this.listener.values().stream().flatMap(List::stream).toList();
        }
        snapshot.forEach(Listener::dispatch);
    }

    /**
     * 设置数据
     *
     * @param data data
     * */
    @SuppressWarnings("unchecked")
    public final void setObject(Object data) {
        this.set(ObjectUtil.parse(data, data == null ? null : (Class<? extends T>) data.getClass()));
    }

    /**
     * 添加监听
     *
     * @param id id
     * @param runnable runnable
     * @param coalesce 仅适用于读取最新状态的 UI 刷新（例如data数据更新，刷新组件）
     * @param initialize 首次添加（异步执行）
     * */
    public final void addListener(Object id, Runnable runnable, boolean coalesce, boolean initialize) {
        if (id == null) {
            id = "DEFAULT";
        }
        if (runnable == null) {
            return;
        }
        Listener registration = new Listener(runnable, coalesce);
        synchronized (this) {
            this.listener.computeIfAbsent(id, _ -> new ArrayList<>()).add(registration);
        }
        if (initialize) {
            registration.dispatch();
        }
    }

    public final void addListener(Object id, Runnable runnable) {
        this.addListener(id, runnable, false, false);
    }

    public final void addListener(Runnable runnable) {
        this.addListener(null, runnable);
    }

    /**
     * 删除监听
     *
     * @param id id
     * */
    public synchronized final void deleteListener(Object id) {
        List<Listener> removed = this.listener.remove(id == null ? "DEFAULT" : id);
        if (removed != null) {
            removed.forEach(registration -> registration.runnable = null);
        }
    }

    private static final class Listener {

        private volatile Runnable runnable;

        private final boolean coalesce;

        private final AtomicBoolean pending = new AtomicBoolean();

        private Listener(Runnable runnable, boolean coalesce) {
            this.runnable = runnable;
            this.coalesce = coalesce;
        }

        private void dispatch() {
            if (this.runnable == null || (this.coalesce && !this.pending.compareAndSet(false, true))) {
                return;
            }
            try {
                Application.asynchronousRun(() -> {
                    this.pending.set(false);
                    Runnable action = this.runnable;
                    if (action != null) {
                        action.run();
                    }
                }, false, true);
            } catch (RuntimeException | Error e) {
                this.pending.set(false);
                throw e;
            }
        }
    }

}