package com.codejune.common.classInfo;

import com.codejune.common.ClassInfo;
import com.codejune.common.DataType;
import com.codejune.common.exception.InfoException;
import java.lang.annotation.Annotation;

/**
 * 字段
 *
 * @author ZJ
 * */
public final class Field {

    private final java.lang.reflect.Field field;

    public Field(java.lang.reflect.Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     * */
    public String getName() {
        return this.field.getName();
    }

    /**
     * 获取原始字段
     *
     * @return 原始字段
     * */
    public java.lang.reflect.Field getOriginField() {
        return this.field;
    }

    /**
     * 获取字段值
     *
     * @param object 对象
     *
     * @return 字段值
     * */
    public Object getData(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return this.field.get(object);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 设置字段值
     *
     * @param object 要设置的对象
     * @param data 要设置的数据
     * */
    public void setData(Object object, Object data) {
        if (object == null) {
            return;
        }
        try {
            this.field.set(object, DataType.parse(data, getClassInfo().getOriginClass()));
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 获取ClassInfo
     *
     * @return ClassInfo
     * */
    public ClassInfo getClassInfo() {
        return new ClassInfo(this.field.getType());
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
        return this.field.isAnnotationPresent(annotationClass);
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
        return this.field.getAnnotation(annotationClass);
    }

    /**
     * 获取类型
     *
     * @return 类型
     * */
    public Class<?> getType() {
        return this.field.getType();
    }

}