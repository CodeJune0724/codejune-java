package com.codejune.common.util;

import com.codejune.common.ClassInfo;
import com.codejune.common.Data;
import com.codejune.common.classinfo.Field;
import com.codejune.common.classinfo.Method;
import com.codejune.common.BaseException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

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
    public static <T> T transform(Object object, Class<T> tClass, boolean builder) {
        return (T) Data.transform(object, tClass, false, builder);
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
    public static <T> T transform(Object object, Class<T> tClass) {
        return transform(object, tClass, true);
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
                return ObjectUtil.transform(data.substring(0, length), object.getClass());
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
        if (object == null) {
            return true;
        }
        if (object instanceof Optional) {
            return ((Optional<?>) object).isEmpty();
        }
        if (object instanceof CharSequence) {
            return ((CharSequence) object).isEmpty();
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
     * @param <T> T
     *
     * @return 克隆的新对象
     * */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T t) {
        if (t == null) {
            return null;
        }
        try {
            return (T) Data.transform(t, t.getClass(), true);
        } catch (Exception e) {
            throw new BaseException(e);
        }
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
        Map<?, ?> o2Map = transform(o2, Map.class);
        if (o1 instanceof Map) {
            Map<Object, Object> o1Map = (Map<Object, Object>) o1;
            o1Map.replaceAll((k, v) -> o2Map.get(k));
        } else if (Data.isObject(o1.getClass())) {
            ClassInfo classInfo = new ClassInfo(o1.getClass());
            for (Field field : classInfo.getFields()) {
                Object setData;
                if (new ClassInfo(field.getType()).isInstanceof(Collection.class)) {
                    setData = Data.transformList(o2Map.get(field.getName()), field.getType(), field.getGenericClass().get(0).getOriginClass(), false);
                } else {
                    setData = transform(o2Map.get(field.getName()), field.getType(), false);
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
                            value = null;
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