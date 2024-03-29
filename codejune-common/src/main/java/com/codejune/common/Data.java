package com.codejune.common;

import com.codejune.common.classinfo.Field;
import com.codejune.common.classinfo.Method;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.JsonUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 数据
 *
 * @author ZJ
 * */
public final class Data {

    /**
     * 数据转换
     *
     * @param object object
     * @param tClass class
     * @param clone 是否是克隆
     * @param builder 是否自动builder
     *
     * @return Object
     * */
    @SuppressWarnings("unchecked")
    public static Object transform(Object object, Class<?> tClass, boolean clone, boolean builder) {
        if (tClass == null || object == null) {
            return null;
        }
        if (tClass == Object.class) {
            return object;
        }
        ClassInfo objectClassInfo = new ClassInfo(object.getClass());
        if (!clone && (object.getClass() == tClass || objectClassInfo.isInstanceof(tClass))) {
            return object;
        }
        ClassInfo tClassClassInfo = new ClassInfo(tClass);
        String objectS = toString(object);
        String tClassName = tClass.getName();
        if (StringUtil.isEmpty(objectS)) {
            return switch (tClassName) {
                case "byte", "int", "long", "short", "char" -> 0;
                case "float", "double" -> 0.0;
                case "boolean" -> false;
                default -> null;
            };
        } else {
            switch (tClassName) {
                case "byte" -> tClass = Byte.class;
                case "int" -> tClass = Integer.class;
                case "long" -> tClass = Long.class;
                case "short" -> tClass = Short.class;
                case "char" -> tClass = Character.class;
                case "float" -> tClass = Float.class;
                case "double" -> tClass = Double.class;
                case "boolean" -> tClass = Boolean.class;
            }
        }
        if (tClass == Byte.class) {
            return Byte.valueOf(objectS);
        }
        if (tClass == Short.class) {
            return Short.valueOf(objectS);
        }
        if (tClass == Integer.class) {
            return Integer.valueOf(objectS);
        }
        if (tClass == Long.class) {
            return Long.valueOf(objectS);
        }
        if (tClass == Float.class) {
            return Float.valueOf(objectS);
        }
        if (tClass == Double.class) {
            if (object instanceof BigDecimal) {
                return object;
            }
            return Double.parseDouble(objectS);
        }
        if (tClass == Character.class) {
            char[] chars = objectS.toCharArray();
            return chars[0];
        }
        if (tClass == String.class) {
            return objectS;
        }
        if (tClass == Boolean.class) {
            return Boolean.valueOf(objectS);
        }
        if (tClassClassInfo.isInstanceof(Collection.class)) {
            return transformList(object, tClass, Object.class, builder);
        }
        if (tClassClassInfo.isInstanceof(Date.class)) {
            Date objectOfDate = null;
            if (object instanceof Date) {
                objectOfDate = (Date) object;
            } else if (object instanceof Number) {
                objectOfDate = new Date((Long) transform(object, Long.class, clone, builder));
            } else if (object instanceof LocalDateTime localDateTime) {
                return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            } else {
                for (String item : DateUtil.COMPATIBLE_DATES) {
                    try {
                        objectOfDate = DateUtil.parse(objectS, item);
                        break;
                    } catch (Exception ignored) {}
                }
            }
            if (objectOfDate == null) {
                throw new BaseException(objectS + "转日期失败");
            }
            Date result = (Date) ObjectUtil.newInstance(tClass);
            result.setTime(objectOfDate.getTime());
            return result;
        }
        if (tClass.isEnum()) {
            for (Object item : tClass.getEnumConstants()) {
                if (item.toString().equals(objectS)) {
                    return item;
                }
            }
            return null;
        }
        if (tClassClassInfo.isInstanceof(Map.class)) {
            if (object instanceof Number) {
                throw new BaseException("object is Number");
            }
            if (object instanceof Boolean) {
                throw new BaseException("object is Boolean");
            }
            if (object instanceof String) {
                return JsonUtil.parse(object, tClass);
            }
            Map<String, Object> result;
            if (tClass == Map.class) {
                result = new LinkedHashMap<>();
            } else {
                result = (Map<String, Object>) ObjectUtil.newInstance(tClass);
            }
            Map<String, Object> objectOfMap;
            if (object instanceof Map) {
                objectOfMap = (Map<String, Object>) object;
            } else {
                objectOfMap = new LinkedHashMap<>();
                ClassInfo classInfo = new ClassInfo(object.getClass());
                for (Field field : classInfo.getFields()) {
                    Object value = field.getData(object);
                    if (!clone) {
                        Method method = classInfo.getGetMethod(field.getName());
                        if (method != null) {
                            try {
                                value = method.execute(object);
                            } catch (Exception e) {
                                value = null;
                            }
                        }
                    }
                    objectOfMap.put(field.getName(), transform(value, field.getType(), clone, builder));
                }
            }
            for (String key : objectOfMap.keySet()) {
                result.put(key, objectOfMap.get(key));
            }
            return result;
        }
        Object result = ObjectUtil.newInstance(tClass);
        if (!clone && builder && (result instanceof Builder builderExe)) {
            builderExe.build(object);
            return builderExe;
        }
        List<Field> fields = tClassClassInfo.getFields();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) transform(object, Map.class, clone, builder)).entrySet()) {
            Object key = entry.getKey();
            if (StringUtil.isEmpty(key)) {
                continue;
            }
            for (Field field : fields) {
                if (key.equals(field.getName())) {
                    Object value;
                    if (new ClassInfo(field.getType()).isInstanceof(Collection.class)) {
                        value = transformList(entry.getValue(), field.getType(), field.getGenericClass().get(0).getOriginClass(), builder);
                    } else {
                        value = transform(entry.getValue(), field.getType(), clone, builder);
                    }
                    boolean isExecuteMethod = false;
                    if (!clone) {
                        Method method = tClassClassInfo.getSetMethod(field.getName());
                        if (method != null) {
                            isExecuteMethod = true;
                            try {
                                method.execute(result, value);
                            } catch (Exception ignored) {}
                        }
                    }
                    if (!isExecuteMethod) {
                        field.setData(result, value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 数据转换
     *
     * @param object object
     * @param tClass class
     * @param clone 是否是克隆
     *
     * @return Object
     * */
    public static Object transform(Object object, Class<?> tClass, boolean clone) {
        return transform(object, tClass, clone, true);
    }

    /**
     * 数据转换
     *
     * @param object object
     * @param tClass class
     *
     * @return Object
     * */
    public static Object transform(Object object, Class<?> tClass) {
        return transform(object, tClass, false);
    }

    /**
     * 转换list
     *
     * @param object object
     * @param tClass tClass
     * @param genericClass genericClass
     * @param builder builder
     *
     * @return Collection<?>
     * */
    @SuppressWarnings("unchecked")
    public static Collection<?> transformList(Object object, Class<?> tClass, Class<?> genericClass, boolean builder) {
        if (object == null) {
            return null;
        }
        if (genericClass == null) {
            genericClass = Object.class;
        }
        if (!new ClassInfo(tClass).isInstanceof(Collection.class)) {
            throw new BaseException(tClass + " is not Collection");
        }
        Collection<Object> result;
        if (tClass == List.class) {
            result = new ArrayList<>();
        } else if (tClass == Set.class) {
            result = new HashSet<>();
        } else {
            result = (Collection<Object>) ObjectUtil.newInstance(tClass);
        }
        Collection<?> objectCollection;
        if (object instanceof Collection<?> collection) {
            objectCollection = collection;
        } else if (object instanceof String) {
            objectCollection = JsonUtil.parse(object, Collection.class);
        } else {
            throw new BaseException(object + " to Collection error");
        }
        if (genericClass == Object.class && object instanceof Collection<?> collection && !collection.isEmpty()) {
            genericClass = collection.toArray()[0].getClass();
        }
        for (Object item : objectCollection) {
            result.add(transform(item, genericClass, true, builder));
        }
        return result;
    }

    /**
     * 转换list
     *
     * @param object object
     * @param tClass tClass
     * @param genericClass genericClass
     *
     * @return Collection<?>
     * */
    public static Collection<?> transformList(Object object, Class<?> tClass, Class<?> genericClass) {
        return transformList(object, tClass, genericClass, true);
    }

    /**
     * toString
     *
     * @param object object
     *
     * @return String
     * */
    public static String toString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Double d) {
            return BigDecimal.valueOf(d).toString();
        }
        if (object instanceof Map<?,?>) {
            return JsonUtil.toJsonString(object);
        }
        return object.toString();
    }

    /**
     * 是否为对象
     *
     * @param aClass aClass
     *
     * @return 是否为对象
     * */
    public static boolean isObject(Class<?> aClass) {
        if (aClass == null) {
            return false;
        }
        if (aClass.isEnum()) {
            return false;
        }
        ClassInfo classInfo = new ClassInfo(aClass);
        if (classInfo.isInstanceof(Number.class)) {
            return false;
        }
        if (classInfo.isInstanceof(Boolean.class)) {
            return false;
        }
        if (classInfo.isInstanceof(String.class)) {
            return false;
        }
        if (classInfo.isInstanceof(Character.class)) {
            return false;
        }
        if (classInfo.isInstanceof(Collection.class)) {
            return false;
        }
        if (classInfo.isInstanceof(Date.class)) {
            return false;
        }
        if (classInfo.isInstanceof(Date.class)) {
            return false;
        }
        return !classInfo.isInstanceof(Map.class);
    }

}