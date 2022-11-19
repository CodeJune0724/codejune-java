package com.codejune.common;

import com.codejune.common.classInfo.Field;
import com.codejune.common.classInfo.Method;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * 数据类型
 *
 * @author ZJ
 * */
public enum DataType {

    /**
     * INT
     * */
    INT(Integer.class),

    /**
     * LONG
     * */
    LONG(Long.class),

    /**
     * DOUBLE
     * */
    DOUBLE(Double.class),

    /**
     * STRING
     * */
    STRING(String.class),

    /**
     * LONG_STRING
     * */
    LONG_STRING(String.class),

    /**
     * BOOLEAN
     * */
    BOOLEAN(Boolean.class),

    /*
     * ----------------------------------------
     * */

    /**
     * 日期
     * */
    DATE(Date.class),

    /**
     * MAP
     * */
    MAP(Map.class),

    /**
     * LIST
     * */
    LIST(List.class),

    /**
     * 枚举
     * */
    ENUM(Object.class),

    /**
     * OBJECT
     * */
    OBJECT(Object.class);

    private final Class<?> aClass;

    DataType(Class<?> aClass) {
        this.aClass = aClass;
    }

    /**
     * 转换
     *
     * @param object object
     * @param dataType dataType
     *
     * @return Object
     * */
    public static Object transform(Object object, DataType dataType) {
        if (dataType == null) {
            return object;
        }
        return transform(object, dataType.aClass);
    }

    /**
     * 转成指定的类
     *
     * @param object object
     * @param tClass class
     *
     * @return Object
     * */
    public static Object transform(Object object, Class<?> tClass) {
        try {
            if (tClass == null || object == null) {
                return null;
            }
            if (tClass == Object.class || new ClassInfo(object.getClass()).isInstanceof(tClass)) {
                return object;
            }
            String objectS = ObjectUtil.toString(object);
            String tClassName = tClass.getName();
            if (StringUtil.isEmpty(objectS)) {
                switch (tClassName) {
                    case "byte":
                    case "int":
                    case "long":
                    case "short":
                    case "char":
                        return 0;
                    case "float":
                    case "double":
                        return 0.0;
                    case "boolean":
                        return false;
                }
                return null;
            } else {
                switch (tClassName) {
                    case "byte":
                        tClass = Byte.class;
                        break;
                    case "int":
                        tClass = Integer.class;
                        break;
                    case "long":
                        tClass = Long.class;
                        break;
                    case "short":
                        tClass = Short.class;
                        break;
                    case "char":
                        tClass = Character.class;
                        break;
                    case "float":
                        tClass = Float.class;
                        break;
                    case "double":
                        tClass = Double.class;
                        break;
                    case "boolean":
                        tClass = Boolean.class;
                        break;
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
            if (new ClassInfo(tClass).isInstanceof(Collection.class)) {
                return JsonUtil.parse(object, tClass);
            }
            if (new ClassInfo(tClass).isInstanceof(Date.class)) {
                Date date = null;
                if (object instanceof Number) {
                    date = new Date(ObjectUtil.transform(object, Long.class));
                } else if (object.getClass() == Date.class) {
                    date = (Date) object;
                } else {
                    for (String s : DateUtil.COMPATIBLE_DATES) {
                        try {
                            date = DateUtil.parse(objectS, s);
                            break;
                        } catch (Exception ignored) {}
                    }
                }
                if (date == null) {
                    throw new InfoException(objectS + "转日期失败");
                }
                if (tClass == Date.class) {
                    return date;
                } else if (tClass == Timestamp.class) {
                    return new Timestamp(date.getTime());
                } else {
                    throw new ErrorException(tClass + "未配置");
                }
            }
            if (new ClassInfo(tClass).isInstanceof(Map.class)) {
                if (object instanceof Number) {
                    throw new InfoException("object is Number");
                }
                if (object instanceof String) {
                    return JsonUtil.parse(object, tClass);
                }
                Map<String, Object> result = new LinkedHashMap<>();
                ClassInfo classInfo = new ClassInfo(object.getClass());
                List<Field> allFields = classInfo.getFields();
                for (Field field : allFields) {
                    Object value = field.getData(object);
                    Method method = classInfo.getMethod(BeanUtil.getGetterMethodName(field.getName(), parse(field.getType())));
                    boolean isExecuteMethod = false;
                    if (method != null) {
                        isExecuteMethod = true;
                        try {
                            value = method.execute(object);
                        } catch (Exception ignored) {}
                    }
                    if (!isExecuteMethod) {
                        result.put(field.getName(), transform(value, field.getType()));
                    }
                }
                return result;
            }
            if (tClass.isEnum()) {
                Object[] enumConstants = tClass.getEnumConstants();
                for (Object o : enumConstants) {
                    if (o.toString().equals(object)) {
                        return o;
                    }
                }
                return null;
            }
            Object result;
            try {
                result = tClass.getConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
            if (result instanceof Builder) {
                ((Builder) result).build(object);
                return result;
            }
            Map<?, ?> objectMap = (Map<?, ?>) transform(object, Map.class);
            if (objectMap == null) {
                throw new InfoException("object is not parse");
            }
            Set<? extends Map.Entry<?, ?>> entries = objectMap.entrySet();
            ClassInfo classInfo = new ClassInfo(tClass);
            List<Field> allFields = classInfo.getFields();
            for (Map.Entry<?, ?> entry : entries) {
                Object key = entry.getKey();
                if (StringUtil.isEmpty(key)) {
                    continue;
                }
                Object value = entry.getValue();
                for (Field field : allFields) {
                    if (key.equals(field.getName())) {
                        value = transform(value, field.getType());
                        Method method = classInfo.getMethod(BeanUtil.getSetterMethodName(field.getName()));
                        boolean isExecuteMethod = false;
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
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 根据class获取datatype
     *
     * @param aClass aClass
     *
     * @return DataType
     * */
    public static DataType parse(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        if (aClass.isEnum()) {
            return ENUM;
        }
        DataType[] values = DataType.values();
        List<DataType> findDataTypeList = new ArrayList<>();
        for (DataType dataType : values) {
            if (dataType == OBJECT) {
                continue;
            }
            if (new ClassInfo(aClass).isInstanceof(dataType.aClass)) {
                findDataTypeList.add(dataType);
            }
        }
        if (findDataTypeList.size() == 0) {
            return DataType.OBJECT;
        }
        return findDataTypeList.get(0);
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
        DataType dataType = DataType.parse(object.getClass());
        if (dataType == DataType.DOUBLE) {
            return BigDecimal.valueOf((Double) object).toString();
        }
        if (dataType == DataType.MAP) {
            return JsonUtil.toJsonString(object);
        }
        return object.toString();
    }

}