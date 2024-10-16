package com.codejune.core.util;

import com.codejune.core.ClassInfo;
import com.codejune.core.Data;
import com.codejune.core.classinfo.Field;
import com.codejune.core.classinfo.Method;
import com.codejune.core.BaseException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;

/**
 * ObjectUtil
 *
 * @author ZJ
 * */
public final class ObjectUtil {

    /**
     * 转成指定的类
     *
     * @param <T> T
     * @param object object
     * @param tClass tClass
     * @param builder builder
     *
     * @return T
     * */
    @SuppressWarnings("unchecked")
    public static <T> T parse(Object object, Class<T> tClass, boolean builder) {
        return (T) Data.parse(object, tClass, builder);
    }

    /**
     * 转成指定的类
     *
     * @param <T> T
     * @param object object
     * @param tClass tClass
     *
     * @return T
     * */
    public static <T> T parse(Object object, Class<T> tClass) {
        return parse(object, tClass, true);
    }

    /**
     * 转成指定的类
     *
     * @param object object
     * @param function function
     * @param <T> T
     *
     * @return T
     * */
    public static <T> T parse(Object object, Function<Object, T> function) {
        if (function == null) {
            throw new BaseException("function is null");
        }
        return function.apply(object);
    }

    /**
     * 截取
     *
     * @param object 数据
     * @param length 截取长度
     *
     * @return Object
     * */
    public static Object subString(Object object, int length) {
        if (object == null) {
            return null;
        }
        Class<?> aClass = object.getClass();
        if (length <= 0) {
            return object;
        }
        if (aClass == String.class || aClass == Double.class || aClass == Integer.class || aClass == Long.class) {
            String data = toString(object);
            if (data != null && data.length() > length) {
                return ObjectUtil.parse(data.substring(0, length), object.getClass());
            }
        }
        return object;
    }

    /**
     * 是否是空
     *
     * @param object object
     *
     * @return 是否为空
     * */
    public static boolean isEmpty(Object object) {
        switch (object) {
            case null -> {
                return true;
            }
            case Optional<?> optional -> {
                return optional.isEmpty();
            }
            case CharSequence charSequence -> {
                return charSequence.isEmpty();
            }
            default -> {}
        }
        if (object.getClass().isArray()) {
            return Array.getLength(object) == 0;
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        }
        if (object instanceof Map) {
            return ((Map<?, ?>) object).isEmpty();
        }
        return false;
    }

    /**
     * toString
     *
     * @param object object
     *
     * @return String
     * */
    public static String toString(Object object) {
        return Data.toString(object);
    }

    /**
     * 克隆
     *
     * @param t t
     * @param strict 严谨模式
     * @param <T> T
     *
     * @return 克隆的新对象
     * */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T t, boolean strict) {
        switch (t) {
            case null -> {
                return null;
            }
            case Cloneable ignored -> {
                Method method = new ClassInfo(t.getClass()).getMethod("clone");
                if (method != null) {
                    return (T) method.execute(t);
                } else {
                    throw new BaseException(t.getClass() + " not cloneable");
                }
            }
            case Map<?, ?> map -> {
                Map<Object, Object> result = (Map<Object, Object>) newInstance(t.getClass());
                map.forEach((key, value) -> result.put(clone(key, false), clone(value, false)));
                return (T) result;
            }
            case Collection<?> collection -> {
                Collection<Object> result = (Collection<Object>) newInstance(t.getClass());
                collection.forEach(item -> result.add(clone(item, false)));
                return (T) result;
            }
            default -> {
                if (strict) {
                    throw new BaseException(t.getClass() + " not cloneable");
                } else {
                    return t;
                }
            }
        }
    }

    /**
     * 克隆
     *
     * @param t t
     * @param <T> T
     *
     * @return 克隆的新对象
     * */
    public static <T> T clone(T t) {
        return clone(t, true);
    }

    /**
     * 赋值
     *
     * @param o1 赋值对象
     * @param o2 取值对象
     * @param <T> 泛型
     *
     * @return o1
     * */
    @SuppressWarnings("unchecked")
    public static <T> T assignment(T o1, Object o2) {
        if (o1 == null || o2 == null) {
            return o1;
        }
        if (!(o2 instanceof Map<?,?>) && !Data.isObject(o2.getClass())) {
            return o1;
        }
        Map<?, ?> o2Map = parse(o2, Map.class);
        if (o1 instanceof Map) {
            Map<Object, Object> o1Map = (Map<Object, Object>) o1;
            o1Map.replaceAll((k, v) -> o2Map.get(k));
        } else if (Data.isObject(o1.getClass())) {
            ClassInfo classInfo = new ClassInfo(o1.getClass());
            for (Field field : classInfo.getField()) {
                Object setData;
                if (new ClassInfo(field.getType()).isInstanceof(Collection.class)) {
                    setData = Data.parseCollection(o2Map.get(field.getName()), field.getType(), field.getGenericClass().getFirst().getJavaClass(), false);
                } else {
                    setData = parse(o2Map.get(field.getName()), field.getType(), false);
                }
                Method setMethod = classInfo.getSetMethod(field.getName());
                if (setMethod != null) {
                    setMethod.execute(o1, setData);
                } else {
                    field.setData(o1, setData);
                }
            }
        }
        return o1;
    }

    /**
     * 实例化对象
     *
     * @param tClass tClass
     * @param <T> T
     *
     * @return T
     * */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> tClass) {
        if (tClass == null) {
            return null;
        }
        if (tClass.isEnum()) {
            T[] enumConstants = tClass.getEnumConstants();
            if (enumConstants.length != 0) {
                return enumConstants[0];
            } else {
                return null;
            }
        }
        Exception error;
        try {
            return tClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            error = e;
            Constructor<?>[] declaredConstructorList = tClass.getDeclaredConstructors();
            for (Constructor<?> constructor : declaredConstructorList) {
                if (constructor.toGenericString().startsWith("public")) {
                    Object[] paramList = new Object[constructor.getParameterCount()];
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class<?> parameterType = parameterTypes[i];
                        Object value;
                        if (parameterType == Object.class) {
                            value = new Object();
                        } else {
                            value = newInstance(parameterType);
                        }
                        paramList[i] = value;
                    }
                    try {
                        return (T) constructor.newInstance(paramList);
                    } catch (Exception exception) {
                        error = exception;
                    }
                }
            }
        }
        throw new BaseException(error.getMessage());
    }

    /**
     * 比较
     *
     * @param o1 o1
     * @param o2 o2
     *
     * @return 是否相等
     * */
    public static boolean equals(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

}