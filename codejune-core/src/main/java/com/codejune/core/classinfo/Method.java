package com.codejune.core.classinfo;

import java.lang.annotation.Annotation;

/**
 * 方法
 *
 * @author ZJ
 * */
public final class Method {

    private final java.lang.reflect.Method method;

    public Method(java.lang.reflect.Method method) {
        this.method = method;
    }

    /**
     * 获取方法名
     *
     * @return 字段名
     * */
    public String getName() {
        return this.method.getName();
    }

    /**
     * 获取原始方法
     *
     * @return 原始方法
     * */
    public java.lang.reflect.Method getOriginMethod() {
        return this.method;
    }

    /**
     * 执行方法
     *
     * @param object 需要执行的对象
     * @param data 参数
     *
     * @return 执行结果
     * */
    public Object execute(Object object, Object... data) {
        try {
            return this.method.invoke(object, data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 是否包含某个注解
     *
     * @param annotationClass annotationClass
     *
     * @return 是否包含某个注解
     * */
    public boolean isAnnotation(Class<? extends Annotation> annotationClass) {
        if (annotationClass == null) {
            return false;
        }
        return this.method.isAnnotationPresent(annotationClass);
    }

    /**
     * 获取注解
     *
     * @param annotationClass annotationClass
     * @param <T> T
     *
     * @return 注解
     * */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.method.getAnnotation(annotationClass);
    }

}