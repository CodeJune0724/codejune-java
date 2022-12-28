package com.codejune.common;

import com.codejune.common.classInfo.Field;
import com.codejune.common.classInfo.Method;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
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

    private final Class<?> aClass;

    public ClassInfo(Type type) {
        this.type = type;
        if (type instanceof Class) {
            this.aClass = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            this.aClass = (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (lowerBounds.length != 0) {
                aClass = (Class<?>) lowerBounds[0];
            } else {
                aClass = (Class<?>) wildcardType.getUpperBounds()[0];
            }
        } else {
            throw new ErrorException("class未配置");
        }
    }

    /**
     * 获取名称
     *
     * @return 名称
     * */
    public String getName() {
        return this.aClass.getName();
    }

    /**
     * 获取原始类
     *
     * @return 原始类
     * */
    public Class<?> getOriginClass() {
        return this.aClass;
    }

    /**
     * 是否存在泛型
     *
     * @return 存在返回true
     * */
    public boolean existsGenericClass() {
        return this.getGenericClass().size() != 0;
    }

    /**
     * 是否存在父级
     *
     * @return 存在返回true
     * */
    public boolean existsSuperClass() {
        return this.getSuperClass().size() != 0;
    }

    /**
     * 获取泛型
     *
     * @return 所有泛型
     * */
    public List<ClassInfo> getGenericClass() {
        List<ClassInfo> result = new ArrayList<>();
        if (this.type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) this.type;
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
        Type superclass = aClass.getGenericSuperclass();
        if (superclass != null) {
            result.add(new ClassInfo(superclass));
        }
        Type[] genericInterfaces = aClass.getGenericInterfaces();
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
            if (classInfo.getOriginClass() == aClass) {
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
        List<java.lang.reflect.Field> fieldList = new ArrayList<>(Arrays.asList(this.aClass.getDeclaredFields()));
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
        List<java.lang.reflect.Method> methodList = new ArrayList<>(Arrays.asList(this.aClass.getMethods()));
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
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(aClass);
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
            throw new InfoException(e);
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
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(aClass);
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
            throw new InfoException(e);
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
        return this.aClass.isAnnotationPresent(annotationClass);
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
        return this.aClass.getAnnotation(annotationClass);
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
        return a.isAssignableFrom(aClass);
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
        Class<?> a = new ClassInfo(type).getOriginClass();
        return a == aClass;
    }

}