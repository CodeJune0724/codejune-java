package com.codejune.core.classinfo;

import com.codejune.core.ClassInfo;
import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public java.lang.reflect.Field getJavaField() {
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
            throw new BaseException(e.getMessage());
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
            ClassInfo classInfo = new ClassInfo(getType());
            List<ClassInfo> genericClass = getGenericClass();
            Object setData;
            if (classInfo.equals(List.class) && !genericClass.isEmpty()) {
                setData = ArrayUtil.parse(ObjectUtil.parse(data, List.class), genericClass.getFirst().getJavaClass());
            } else if (classInfo.equals(Map.class) && genericClass.size() >= 2) {
                setData = MapUtil.parse(data, genericClass.getFirst().getJavaClass(), genericClass.get(1).getJavaClass());
            } else {
                setData = ObjectUtil.parse(data, classInfo.getJavaClass());
            }
            this.field.set(object, setData);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 获取类型
     *
     * @return ClassInfo
     * */
    public Class<?> getType() {
        return this.field.getType();
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
     * 获取泛型
     *
     * @return 所有泛型
     * */
    public List<ClassInfo> getGenericClass() {
        List<ClassInfo> result = new ArrayList<>();
        Type genericType = this.field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type type : actualTypeArguments) {
                result.add(new ClassInfo(type));
            }
        }
        return result;
    }

}