package com.codejune.core;

import com.codejune.core.classinfo.Field;
import com.codejune.core.classinfo.Method;
import com.codejune.core.util.DateUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
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
     * @param builder 是否自动builder
     *
     * @return Object
     * */
    @SuppressWarnings("unchecked")
    public static Object parse(Object object, Class<?> tClass, boolean builder) {
        if (tClass == null || object == null) {
            return null;
        }
        if (tClass == Object.class) {
            return object;
        }
        ClassInfo objectClassInfo = new ClassInfo(object.getClass());
        if (object.getClass() == tClass || objectClassInfo.isInstanceof(tClass)) {
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
            return parseCollection(object, tClass, Object.class, builder);
        }
        if (tClassClassInfo.isInstanceof(Date.class)) {
            Date objectOfDate = null;
            switch (object) {
                case Date date -> objectOfDate = date;
                case Number number -> objectOfDate = new Date((Long) parse(number, Long.class, builder));
                case LocalDateTime localDateTime -> objectOfDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                case LocalDate localDate -> objectOfDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                default -> {
                    for (String item : DateUtil.COMPATIBLE_DATE_LIST) {
                        try {
                            objectOfDate = new SimpleDateFormat(item).parse(objectS);
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
        if (tClass == LocalDateTime.class) {
            switch (object) {
                case Date date -> {
                    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                }
                case Number number -> {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) parse(number, Long.class)), ZoneId.systemDefault());
                }
                case LocalDate localDate -> {
                    return localDate.atStartOfDay(ZoneId.systemDefault());
                }
                case String string -> {
                    LocalDateTime result = null;
                    for (String item : DateUtil.COMPATIBLE_DATE_LIST) {
                        try {
                            result = DateUtil.parse(string, item, LocalDateTime.class);
                            break;
                        } catch (Exception ignored) {}
                    }
                    if (result == null) {
                        throw new BaseException(string + "转日期失败");
                    }
                    return result;
                }
                default -> throw new BaseException(objectS + "转日期失败");
            }
        }
        if (tClass == LocalDate.class) {
            switch (object) {
                case Date date -> {
                    return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
                }
                case Number number -> {
                    return LocalDate.ofInstant(Instant.ofEpochMilli((Long) parse(number, Long.class)), ZoneId.systemDefault());
                }
                case LocalDateTime localDateTime -> {
                    return localDateTime.toLocalDate();
                }
                case String string -> {
                    LocalDate result = null;
                    for (String item : DateUtil.COMPATIBLE_DATE_LIST) {
                        try {
                            result = DateUtil.parse(string,item, LocalDate.class);
                            break;
                        } catch (Exception ignored) {}
                    }
                    if (result == null) {
                        throw new BaseException(string + "转日期失败");
                    }
                    return result;
                }
                default -> throw new BaseException(objectS + "转日期失败");
            }
        }
        if (tClass == LocalTime.class) {
            switch (object) {
                case Date date -> {
                    return LocalTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                }
                case Number number -> {
                    return LocalTime.ofInstant(Instant.ofEpochMilli((Long) parse(number, Long.class)), ZoneId.systemDefault());
                }
                case LocalDateTime localDateTime -> {
                    return localDateTime.toLocalTime();
                }
                case String string -> {
                    LocalTime result = null;
                    for (String item : DateUtil.COMPATIBLE_DATE_LIST) {
                        try {
                            result = DateUtil.parse(string, item, LocalTime.class);
                            break;
                        } catch (Exception ignored) {}
                    }
                    if (result == null) {
                        throw new BaseException(string + "转日期失败");
                    }
                    return result;
                }
                default -> throw new BaseException(objectS + "转日期失败");
            }
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
                for (Field field : classInfo.getField()) {
                    Object value = field.getData(object);
                    Method method = classInfo.getGetMethod(field.getName());
                    if (method != null) {
                        try {
                            value = method.execute(object);
                        } catch (Exception e) {
                            value = null;
                        }
                    }
                    Class<?> type = field.getType();
                    objectOfMap.put(field.getName(), parse(value, type != Object.class ? type : value == null ? type : value.getClass(), builder));
                }
            }
            for (String key : objectOfMap.keySet()) {
                result.put(key, objectOfMap.get(key));
            }
            return result;
        }
        Object result = ObjectUtil.newInstance(tClass);
        if (builder && (result instanceof Builder builderExe)) {
            builderExe.build(object);
            return builderExe;
        }
        List<Field> fields = tClassClassInfo.getField();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) parse(object, Map.class, builder)).entrySet()) {
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
                        value = parseCollection(entryValue, type != Object.class ? type : entryValue == null ? type : entryValue.getClass(), field.getGenericClass().getFirst().getJavaClass(), builder);
                    } else {
                        Object entryValue = entry.getValue();
                        value = parse(entryValue, type != Object.class ? type : entryValue == null ? type : entryValue.getClass(), builder);
                    }
                    boolean isExecuteMethod = false;
                    Method method = tClassClassInfo.getSetMethod(field.getName());
                    if (method != null) {
                        isExecuteMethod = true;
                        try {
                            method.execute(result, value);
                        } catch (Exception ignored) {}
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
     *
     * @return Object
     * */
    public static Object parse(Object object, Class<?> tClass) {
        return parse(object, tClass, true);
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
    public static Collection<?> parseCollection(Object object, Class<?> tClass, Class<?> genericClass, boolean builder) {
        if (!(object instanceof Collection<?>)) {
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
                result.add(parse(item, genericClass, builder));
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
    public static Collection<?> parseCollection(Object object, Class<?> tClass, Class<?> genericClass) {
        return parseCollection(object, tClass, genericClass, true);
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