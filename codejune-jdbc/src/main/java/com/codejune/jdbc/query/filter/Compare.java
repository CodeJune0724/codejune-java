package com.codejune.jdbc.query.filter;

import com.codejune.core.Builder;
import com.codejune.core.BaseException;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 比较
 *
 * @author ZJ
 * */
public final class Compare implements Builder, Cloneable {

    private Type type;

    private String key;

    private Object value;

    public Compare(Type type, Object key, Object value) {
        this.type = type;
        this.key = ObjectUtil.toString(key);
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public void build(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof Compare compare) {
            ObjectUtil.assignment(this, compare);
            return;
        }
        Map<String, Object> map = MapUtil.parse(object, String.class, Object.class);
        if (map == null) {
            map = new HashMap<>();
        }
        if (map.size() != 1) {
            throw new BaseException("Compare build error");
        }
        for (String key : map.keySet()) {
            Object value = map.get(key);
            Type type = Type.EQUALS;
            Object setValue = value;
            if (value instanceof Map<?,?> valueMap) {
                for (Object valueMapKey : valueMap.keySet()) {
                    type = Type.getType(ObjectUtil.toString(valueMapKey));
                    setValue = valueMap.get(valueMapKey);
                    break;
                }
            }
            this.type = type;
            this.key = key;
            this.value = setValue;
            break;
        }
    }

    @Override
    public Compare clone() {
        try {
            Compare result = (Compare) super.clone();
            result.type = this.type;
            result.key = this.key;
            result.value = ObjectUtil.clone(this.value, false);
            return result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 大于
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare gt(Object key, Object value) {
        return new Compare(Type.GT, key, value);
    }

    /**
     * 大于
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare gt(Class<T> tClass, Function<T, ?> Function, Object value) {
        return gt(getColumnName(tClass, Function), value);
    }

    /**
     * 大于等于
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare gte(Object key, Object value) {
        return new Compare(Type.GTE, key, value);
    }

    /**
     * 大于等于
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare gte(Class<T> tClass, Function<T, ?> Function, Object value) {
        return gte(getColumnName(tClass, Function), value);
    }

    /**
     * 小于
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare lt(Object key, Object value) {
        return new Compare(Type.LT, key, value);
    }

    /**
     * 小于
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare lt(Class<T> tClass, Function<T, ?> Function, Object value) {
        return lt(getColumnName(tClass, Function), value);
    }

    /**
     * 小于等于
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare lte(Object key, Object value) {
        return new Compare(Type.LTE, key, value);
    }

    /**
     * 小于等于
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare lte(Class<T> tClass, Function<T, ?> Function, Object value) {
        return lte(getColumnName(tClass, Function), value);
    }

    /**
     * 等于
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare equals(Object key, Object value) {
        return new Compare(Type.EQUALS, key, value);
    }

    /**
     * 等于
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare equals(Class<T> tClass, Function<T, ?> Function, Object value) {
        return equals(getColumnName(tClass, Function), value);
    }

    /**
     * 不等于
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare notEquals(Object key, Object value) {
        return new Compare(Type.NOT_EQUALS, key, value);
    }

    /**
     * 不等于
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare notEquals(Class<T> tClass, Function<T, ?> Function, Object value) {
        return notEquals(getColumnName(tClass, Function), value);
    }

    /**
     * in
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare in(Object key, Object value) {
        return new Compare(Type.IN, key, value);
    }

    /**
     * in
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare in(Class<T> tClass, Function<T, ?> Function, Object value) {
        return in(getColumnName(tClass, Function), value);
    }

    /**
     * notIn
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare notIn(Object key, Object value) {
        return new Compare(Type.NOT_IN, key, value);
    }

    /**
     * notIn
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare notIn(Class<T> tClass, Function<T, ?> Function, Object value) {
        return notIn(getColumnName(tClass, Function), value);
    }

    /**
     * 包含
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare contains(Object key, Object value) {
        return new Compare(Type.CONTAINS, key, value);
    }

    /**
     * 包含
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare contains(Class<T> tClass, Function<T, ?> Function, Object value) {
        return contains(getColumnName(tClass, Function), value);
    }

    /**
     * 不包含
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare notContains(Object key, Object value) {
        return new Compare(Type.NOT_CONTAINS, key, value);
    }

    /**
     * 不包含
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare notContains(Class<T> tClass, Function<T, ?> Function, Object value) {
        return notContains(getColumnName(tClass, Function), value);
    }

    /**
     * 以...开头
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare startWith(Object key, Object value) {
        return new Compare(Type.START_WITH, key, value);
    }

    /**
     * 以...开头
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare startWith(Class<T> tClass, Function<T, ?> Function, Object value) {
        return startWith(getColumnName(tClass, Function), value);
    }

    /**
     * 不以...开头
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare notStartWith(Object key, Object value) {
        return new Compare(Type.NOT_START_WITH, key, value);
    }

    /**
     * 不以...开头
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare notStartWith(Class<T> tClass, Function<T, ?> Function, Object value) {
        return notStartWith(getColumnName(tClass, Function), value);
    }

    /**
     * 以...结尾
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare endWith(Object key, Object value) {
        return new Compare(Type.END_WITH, key, value);
    }

    /**
     * 以...结尾
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare endWith(Class<T> tClass, Function<T, ?> Function, Object value) {
        return endWith(getColumnName(tClass, Function), value);
    }

    /**
     * 不以...结尾
     *
     * @param key key
     * @param value value
     *
     * @return Compare
     * */
    public static Compare notEndWith(Object key, Object value) {
        return new Compare(Type.NOT_END_WITH, key, value);
    }

    /**
     * 不以...结尾
     *
     * @param tClass tClass
     * @param Function Function
     * @param value value
     * @param <T> T
     *
     * @return Compare
     * */
    public static <T> Compare notEndWith(Class<T> tClass, Function<T, ?> Function, Object value) {
        return notEndWith(getColumnName(tClass, Function), value);
    }

    private static <T> String getColumnName(Class<T> tClass, Function<T, ?> function) {
        if (tClass == null || function == null) {
            throw new BaseException("getFieldName() param error");
        }
        String result;
        try {
            Method method = function.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            result = ((SerializedLambda) method.invoke(function)).getImplMethodName();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        if (result.startsWith("is")) {
            result = result.substring(2);
        } else {
            if (!result.startsWith("get") && !result.startsWith("set")) {
                throw new BaseException("Error parsing property name '" + result + "'.  Didn't start with 'is', 'get' or 'set'.");
            }
            result = result.substring(3);
        }
        if (result.length() == 1 || result.length() > 1 && !Character.isUpperCase(result.charAt(1))) {
            result = result.substring(0, 1).toLowerCase(Locale.ENGLISH) + result.substring(1);
        }
        return result;
    }

    /**
     * 类型
     *
     * @author ZJ
     * */
    public enum Type {

        GT("$gt"),

        GTE("$gte"),

        LT("$lt"),

        LTE("$lte"),

        EQUALS("$equals"),

        NOT_EQUALS("$!equals"),

        IN("$in"),

        NOT_IN("$!in"),

        CONTAINS("$contains"),

        NOT_CONTAINS("$!contains"),

        START_WITH("$startWith"),

        NOT_START_WITH("$!startWith"),

        END_WITH("$endWith"),

        NOT_END_WITH("$!endWith");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        private static Type getType(String value) {
            for (Type typeItem : Type.values()) {
                if (typeItem.value.equals(value)) {
                    return typeItem;
                }
            }
            throw new BaseException(value + " is not transform Type");
        }

    }

    public interface Function<T, R> extends java.util.function.Function<T,R>, Serializable {}

}