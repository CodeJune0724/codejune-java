package com.codejune.common.util;

import com.codejune.common.ClassInfo;
import com.codejune.common.DataType;
import com.codejune.common.classInfo.Field;
import com.codejune.common.exception.InfoException;
import java.lang.reflect.Array;
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
     *
     * @return T
     * */
    @SuppressWarnings("unchecked")
    public static <T> T transform(Object object, Class<T> tClass) {
        return (T) DataType.transform(object, tClass);
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
        if (aClass == null) {
            return object;
        }
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
            return !((Optional<?>) object).isPresent();
        }
        if (object instanceof CharSequence) {
            return ((CharSequence) object).length() == 0;
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
        return DataType.toString(object);
    }

    /**
     * 克隆
     *
     * @param t t
     * @param isForceInstance 是否强制实例化
     * @param <T> T
     *
     * @return 克隆的新对象
     * */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T t, boolean isForceInstance) {
        if (t == null) {
            return null;
        }
        DataType dataType = DataType.toDataType(t.getClass());
        String tString = ObjectUtil.toString(t);
        if (StringUtil.isEmpty(tString)) {
            return t;
        }
        switch (dataType) {
            case INT:
                return (T) Integer.valueOf(tString);
            case LONG:
                return (T) Long.valueOf(tString);
            case DOUBLE:
                return (T) Double.valueOf(tString);
            case STRING:
                return (T) tString;
            case BOOLEAN:
                return (T) Boolean.valueOf(tString);
            case DATE:
                Date date = (Date) t;
                return (T) new Date(date.getTime());
            case LIST:
                List<?> list = (List<?>) t;
                List<Object> result = new ArrayList<>();
                for (Object item : list) {
                    result.add(clone(item, isForceInstance));
                }
                return (T) result;
            case OBJECT:
                Object re;
                try {
                    re = t.getClass().getConstructor().newInstance();
                } catch (Exception e) {
                    if (isForceInstance) {
                        throw new InfoException("实例化失败: " + e.getMessage());
                    } else {
                        return t;
                    }
                }
                ClassInfo classInfo = new ClassInfo(re.getClass());
                List<Field> allFields = classInfo.getFields();
                for (Field field : allFields) {
                    field.setData(re, clone(field.getData(t), isForceInstance));
                }
                return (T) re;
            case MAP:
                Map<?, ?> tMap = (Map<?, ?>) t;
                Map<Object, Object> map = new HashMap<>();
                Set<?> keySet = tMap.keySet();
                for (Object key : keySet) {
                    map.put(key, clone(tMap.get(key), isForceInstance));
                }
                return (T) map;
        }
        return t;
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
     * */
    @SuppressWarnings("unchecked")
    public static void assignment(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return;
        }
        DataType o1DataType = DataType.toDataType(o1.getClass());
        DataType o2DataType = DataType.toDataType(o2.getClass());
        if (o2DataType != DataType.MAP && o2DataType != DataType.OBJECT) {
            return;
        }
        Map<?, ?> o2Map = transform(o2, Map.class);
        switch (o1DataType) {
            case MAP:
                Map<Object, Object> o1Map = (Map<Object, Object>) o1;
                Set<?> keySet = o1Map.keySet();
                for (Object key : keySet) {
                    o1Map.put(key, o2Map.get(key));
                }
            case OBJECT:
                ClassInfo classInfo = new ClassInfo(o1.getClass());
                List<Field> fields = classInfo.getFields();
                for (Field field : fields) {
                    field.setData(o1, o2Map.get(field.getName()));
                }
        }
    }

}