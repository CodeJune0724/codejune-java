package com.codejune.core;

import com.codejune.core.classinfo.Field;
import com.codejune.core.classinfo.Method;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * class信息
 *
 * @author ZJ
 * */
public final class ClassInfo {

    private final Type type;

    private final Class<?> rawClass;

    public ClassInfo(Type type) {
        this.type = type;
        switch (type) {
            case Class<?> typeOfClass -> this.rawClass = typeOfClass;
            case ParameterizedType parameterizedType -> this.rawClass = (Class<?>) parameterizedType.getRawType();
            case WildcardType wildcardType -> {
                Type[] lowerBounds = wildcardType.getLowerBounds();
                if (lowerBounds.length != 0) {
                    rawClass = (Class<?>) lowerBounds[0];
                } else {
                    rawClass = (Class<?>) wildcardType.getUpperBounds()[0];
                }
            }
            case null, default -> throw new BaseException("class未配置");
        }
    }

    public Type getType() {
        return type;
    }

    public Class<?> getRawClass() {
        return rawClass;
    }

    /**
     * 获取名称
     *
     * @return 名称
     * */
    public String getName() {
        return this.rawClass.getName();
    }

    /**
     * 是否存在泛型
     *
     * @return 存在返回true
     * */
    public boolean existsGenericClass() {
        return !this.getGenericClass().isEmpty();
    }

    /**
     * 是否存在父级
     *
     * @return 存在返回true
     * */
    public boolean existsSuperClass() {
        return !this.getSuperClass().isEmpty();
    }

    /**
     * 获取泛型
     *
     * @return 所有泛型
     * */
    public List<ClassInfo> getGenericClass() {
        List<ClassInfo> result = new ArrayList<>();
        if (this.type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type type : actualTypeArguments) {
                result.add(new ClassInfo(type));
            }
        }
        return result;
    }

    /**
     * 获取父类
     *
     * @return 所有父类
     * */
    public List<ClassInfo> getSuperClass() {
        List<ClassInfo> result = new ArrayList<>();
        Type superclass = rawClass.getGenericSuperclass();
        if (superclass != null) {
            result.add(new ClassInfo(superclass));
        }
        Type[] genericInterfaces = rawClass.getGenericInterfaces();
        for (Type type : genericInterfaces) {
            result.add(new ClassInfo(type));
        }
        return result;
    }

    /**
     * 查找指定父类
     *
     * @param aClass 查找的父类
     *
     * @return 找到的父类
     * */
    public ClassInfo getSuperClass(Class<?> aClass) {
        if (!this.existsSuperClass() || aClass == null) {
            return null;
        }
        for (ClassInfo classInfo : this.getSuperClass()) {
            if (classInfo.getRawClass() == aClass) {
                return classInfo;
            }
            ClassInfo superClass = classInfo.getSuperClass(aClass);
            if (superClass != null) {
                return superClass;
            }
        }
        return null;
    }

    /**
     * 获取字段
     *
     * @return 所有字段
     * */
    public List<Field> getFields() {
        List<Field> result = new ArrayList<>();
        List<java.lang.reflect.Field> fieldList = new ArrayList<>(Arrays.asList(this.rawClass.getDeclaredFields()));
        List<ClassInfo> superClass = getSuperClass();
        for (ClassInfo classInfo : superClass) {
            result.addAll(classInfo.getFields());
        }
        for (java.lang.reflect.Field field : fieldList) {
            result.add(new Field(field));
        }
        return result;
    }

    /**
     * 获取字段
     *
     * @param fieldName 字段名
     *
     * @return 字段
     * */
    public Field getField(String fieldName) {
        List<Field> fieldList = getFields();
        if (ObjectUtil.isEmpty(fieldList) || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        for (Field field : fieldList) {
            if (fieldName.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    /**
     * 获取方法
     *
     * @return 所有方法
     * */
    public List<Method> getMethods() {
        List<Method> result = new ArrayList<>();
        List<java.lang.reflect.Method> methodList = new ArrayList<>(Arrays.asList(this.rawClass.getMethods()));
        List<ClassInfo> superClass = getSuperClass();
        for (ClassInfo classInfo : superClass) {
            result.addAll(classInfo.getMethods());
        }
        for (java.lang.reflect.Method method : methodList) {
            result.add(new Method(method));
        }
        return result;
    }

    /**
     * 获取方法
     *
     * @param methodName 方法名
     *
     * @return 方法
     * */
    public Method getMethod(String methodName) {
        List<Method> methodList = getMethods();
        if (ObjectUtil.isEmpty(methodList) || StringUtil.isEmpty(methodName)) {
            return null;
        }
        for (Method method : methodList) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    /**
     * 获取get方法
     *
     * @param fieldName 字段名
     *
     * @return Method
     * */
    public Method getGetMethod(String fieldName) {
        if (StringUtil.isEmpty(fieldName)) {
            return null;
        }
        Method result;
        try {
            result = new Method(rawClass.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)));
        } catch (Exception e) {
            result = null;
        }
        if (result != null) {
            return result;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(rawClass);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getName().equals(fieldName)) {
                    java.lang.reflect.Method readMethod = propertyDescriptor.getReadMethod();
                    if (readMethod != null) {
                        return getMethod(readMethod.getName());
                    }
                    break;
                }
            }
            return null;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取set方法
     *
     * @param fieldName 字段名
     *
     * @return Method
     * */
    public Method getSetMethod(String fieldName) {
        if (StringUtil.isEmpty(fieldName)) {
            return null;
        }
        Field field = getField(fieldName);
        if (field == null) {
            return null;
        }
        Method result;
        try {
            result = new Method(rawClass.getMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), field.getType()));
        } catch (Exception e) {
            result = null;
        }
        if (result != null) {
            return result;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(rawClass);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (propertyDescriptor.getName().equals(fieldName)) {
                    java.lang.reflect.Method writeMethod = propertyDescriptor.getWriteMethod();
                    if (writeMethod != null) {
                        return getMethod(writeMethod.getName());
                    }
                    break;
                }
            }
            return null;
        } catch (Exception e) {
            throw new BaseException(e);
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
        return this.rawClass.isAnnotationPresent(annotationClass);
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
        return this.rawClass.getAnnotation(annotationClass);
    }

    /**
     * 属于a类
     *
     * @param a a类
     *
     * @return 属于返回true
     * */
    public boolean isInstanceof(Class<?> a) {
        if (a == null || a == Object.class) {
            return false;
        }
        return a.isAssignableFrom(rawClass);
    }

    /**
     * 是否相等
     *
     * @param type type
     *
     * @return 相等返回true
     * */
    public boolean equals(Type type) {
        if (type == null) {
            return false;
        }
        Class<?> a = new ClassInfo(type).getRawClass();
        return a == rawClass;
    }

}