package com.codejune.core;

import com.codejune.core.classinfo.Field;
import com.codejune.core.classinfo.Method;
import com.codejune.core.util.DateUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
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
            if (object instanceof BigDecimal bigDecimal) {
                return bigDecimal.doubleValue();
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
            return transformCollection(object, tClass, Object.class, builder);
        }
        if (tClassClassInfo.isInstanceof(Date.class)) {
            Date objectOfDate = null;
            switch (object) {
                case Date date -> objectOfDate = date;
                case Number number -> objectOfDate = new Date((Long) transform(number, Long.class, clone, builder));
                case LocalDateTime localDateTime -> {
                    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                }
                default -> {
                    for (String item : DateUtil.COMPATIBLE_DATES) {
                        try {
                            objectOfDate = DateUtil.parse(objectS, item);
                            break;
                        } catch (Exception ignored) {}
                    }
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
                    Class<?> type = field.getType();
                    objectOfMap.put(field.getName(), transform(value, type != Object.class ? type : value == null ? type : value.getClass(), clone, builder));
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
                    Class<?> type = field.getType();
                    Object value;
                    if (new ClassInfo(type).isInstanceof(Collection.class)) {
                        Object entryValue = entry.getValue();
                        value = transformCollection(entryValue, type != Object.class ? type : entryValue == null ? type : entryValue.getClass(), field.getGenericClass().getFirst().getRawClass(), builder);
                    } else {
                        Object entryValue = entry.getValue();
                        value = transform(entryValue, type != Object.class ? type : entryValue == null ? type : entryValue.getClass(), clone, builder);
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
    public static Collection<?> transformCollection(Object object, Class<?> tClass, Class<?> genericClass, boolean builder) {
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
        if (genericClass == Object.class && object instanceof Collection<?> collection && !collection.isEmpty()) {
            genericClass = collection.toArray()[0].getClass();
        }
        if (object instanceof Collection<?> collection) {
            for (Object item : collection) {
                result.add(transform(item, genericClass, true, builder));
            }
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
    public static Collection<?> transformCollection(Object object, Class<?> tClass, Class<?> genericClass) {
        return transformCollection(object, tClass, genericClass, true);
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