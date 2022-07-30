package com.codejune.common.model;

import com.codejune.common.ModelAble;
import com.codejune.common.classInfo.Field;
import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.KeyHandler;
import com.codejune.common.util.ObjectUtil;
import java.util.*;

/**
 * 过滤
 *
 * @author ZJ
 * */
public final class Filter implements ModelAble<Filter> {

    private final List<Filter> or = new ArrayList<>();

    private final List<Item> and = new ArrayList<>();

    private Config config;

    public List<Filter> getOr() {
        return or;
    }

    public List<Item> getAnd() {
        if (config != null) {
            if (config.isCleanNull()) {
                this.cleanNull();
            }
        }
        return and;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * 或者
     *
     * @param filter filter
     *
     * @return Filter
     * */
    public Filter or(Filter filter) {
        if (filter == null) {
            return this;
        }
        if (!ObjectUtil.isEmpty(filter.or)) {
            throw new InfoException("or禁止包含or");
        }
        or.add(filter);
        return this;
    }

    /**
     * 并且
     *
     * @param item item
     *
     * @return Filter
     * */
    public Filter and(Item item) {
        if (item != null) {
            and.add(item);
        }
        return this;
    }

    /**
     * 设置新的key
     *
     * @param keyHandler keyHandler
     * */
    public void setKey(KeyHandler keyHandler) {
        if (keyHandler == null) {
            return;
        }
        for (Filter filter : or) {
            filter.setKey(keyHandler);
        }
        for (Item item : and) {
            item.key = ObjectUtil.toString(keyHandler.getNewKey(item.key));
        }
    }

    /**
     * 清空null
     *
     * @return Filter
     * */
    public Filter cleanNull() {
        for (Filter filter : this.or) {
            filter.cleanNull();
        }
        List<Item> isNotNull = new ArrayList<>();
        for (Item item : this.and) {
            if (!ObjectUtil.isEmpty(item.getValue())) {
                isNotNull.add(item);
            }
        }
        this.and.clear();
        this.and.addAll(isNotNull);
        return this;
    }

    /**
     * 过滤key
     *
     * @param keyList 需要存在的key
     *
     * @return Filter
     * */
    public Filter filterKey(Collection<?> keyList) {
        if (ObjectUtil.isEmpty(keyList)) {
            return this;
        }
        List<String> strings = new ArrayList<>();
        for (Object o : keyList) {
            if (o instanceof Column) {
                strings.add(((Column) o).getName());
            } else if (o instanceof java.lang.reflect.Field) {
                strings.add(((java.lang.reflect.Field) o).getName());
            } else if (o instanceof String) {
                strings.add(ObjectUtil.toString(o));
            } else if (o instanceof Field) {
                strings.add(((Field) o).getName());
            }
        }
        if (ObjectUtil.isEmpty(strings)) {
            return this;
        }
        for (Filter filter : this.or) {
            filter.filterKey(keyList);
        }
        List<Item> list = new ArrayList<>();
        for (Item item : this.and) {
            if (strings.contains(item.getKey())) {
                list.add(item);
            }
        }
        this.and.clear();
        this.and.addAll(list);
        return this;
    }

    @Override
    public Filter assignment(Object object) {
        if (object == null) {
            return null;
        }
        Map<String, Object> map = ObjectUtil.parseMap(object, String.class, Object.class);
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            Object value = map.get(key);
            if ("$config".equals(key)) {
                this.setConfig(ObjectUtil.parse(value, Config.class));
            } else if ("$or".equals(key)) {
                for (Object map1 : (List<?>) value) {
                    this.or(new Filter().assignment(ObjectUtil.parseMap(map1, String.class, Object.class)));
                }
            } else {
                if (value instanceof Map) {
                    Map<String, Object> map1 = ObjectUtil.parseMap(value, String.class, Object.class);
                    Set<String> keySet1 = map1.keySet();
                    for (String key1 : keySet1) {
                        Item.Type type = null;
                        Item.Type[] values = Item.Type.values();
                        for (Item.Type type1 : values) {
                            if (type1.value.equals(key1)) {
                                type = type1;
                                break;
                            }
                        }
                        if (type == null) {
                            continue;
                        }
                        Object value1 = map1.get(key1);
                        this.and(new Item(type, key, value1));
                    }
                } else {
                    this.and(Item.equals(key, value));
                }
            }
        }
        return this;
    }

    /**
     * 转换
     *
     * @param object object
     *
     * @return Filter
     * */
    public static Filter parse(Object object) {
        return new Filter().assignment(object);
    }

    /**
     * 校验单独项
     *
     * @author ZJ
     * */
    public static final class Item {

        private final Type type;

        private String key;

        private Object value;

        public Item() {
            this(null, null, null);
        }

        public Item(Type type, Object key, Object value) {
            this.type = type;
            this.key = ObjectUtil.toString(key);
            this.value = value;
        }

        public Type getType() {
            return type;
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
        public static Item gt(Object key, Object value) {
            return new Item(Type.GT, key, value);
        }

        /**
         * 大于等于
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item gte(Object key, Object value) {
            return new Item(Type.GTE, key, value);
        }

        /**
         * 小于
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item lt(Object key, Object value) {
            return new Item(Type.LT, key, value);
        }

        /**
         * 小于等于
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item lte(Object key, Object value) {
            return new Item(Type.LTE, key, value);
        }

        /**
         * 等于
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item equals(Object key, Object value) {
            return new Item(Type.EQUALS, key, value);
        }

        /**
         * 不等于
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item notEquals(Object key, Object value) {
            return new Item(Type.NOT_EQUALS, key, value);
        }

        /**
         * in
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item in(Object key, Object value) {
            return new Item(Type.IN, key, value);
        }

        /**
         * notIn
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item notIn(Object key, Object value) {
            return new Item(Type.NOT_IN, key, value);
        }

        /**
         * 包含
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item contains(Object key, Object value) {
            return new Item(Type.CONTAINS, key, value);
        }

        /**
         * 不包含
         *
         * @param key key
         * @param value value
         *
         * @return Item
         * */
        public static Item notContains(Object key, Object value) {
            return new Item(Type.NOT_CONTAINS, key, value);
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

            NOT_CONTAINS("$!contains");

            private final String value;

            Type(String value) {
                this.value = value;
            }

        }

    }

    /**
     * 配置
     *
     * @author ZJ
     * */
    public static final class Config {

        private boolean cleanNull;

        public boolean isCleanNull() {
            return cleanNull;
        }

        public void setCleanNull(boolean cleanNull) {
            this.cleanNull = cleanNull;
        }

    }

}