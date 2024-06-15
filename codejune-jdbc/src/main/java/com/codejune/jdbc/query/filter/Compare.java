package com.codejune.jdbc.query.filter;

import com.codejune.core.Builder;
import com.codejune.core.BaseException;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * 比较
 *
 * @author ZJ
 * */
public final class Compare implements Builder {

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

    /**
     * 大于
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare gt(Object key, Object value) {
        return new Compare(Type.GT, key, value);
    }

    /**
     * 大于等于
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare gte(Object key, Object value) {
        return new Compare(Type.GTE, key, value);
    }

    /**
     * 小于
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare lt(Object key, Object value) {
        return new Compare(Type.LT, key, value);
    }

    /**
     * 小于等于
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare lte(Object key, Object value) {
        return new Compare(Type.LTE, key, value);
    }

    /**
     * 等于
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare equals(Object key, Object value) {
        return new Compare(Type.EQUALS, key, value);
    }

    /**
     * 不等于
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare notEquals(Object key, Object value) {
        return new Compare(Type.NOT_EQUALS, key, value);
    }

    /**
     * in
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare in(Object key, Object value) {
        return new Compare(Type.IN, key, value);
    }

    /**
     * notIn
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare notIn(Object key, Object value) {
        return new Compare(Type.NOT_IN, key, value);
    }

    /**
     * 包含
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare contains(Object key, Object value) {
        return new Compare(Type.CONTAINS, key, value);
    }

    /**
     * 不包含
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare notContains(Object key, Object value) {
        return new Compare(Type.NOT_CONTAINS, key, value);
    }

    /**
     * 以...开头
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare startWith(Object key, Object value) {
        return new Compare(Type.START_WITH, key, value);
    }

    /**
     * 不以...开头
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare notStartWith(Object key, Object value) {
        return new Compare(Type.NOT_START_WITH, key, value);
    }

    /**
     * 以...结尾
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare endWith(Object key, Object value) {
        return new Compare(Type.END_WITH, key, value);
    }

    /**
     * 不以...结尾
     *
     * @param key key
     * @param value value
     *
     * @return Item
     * */
    public static Compare notEndWith(Object key, Object value) {
        return new Compare(Type.NOT_END_WITH, key, value);
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

}