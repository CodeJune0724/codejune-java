package com.codejune.jdbc;

import com.codejune.common.Builder;
import com.codejune.common.Data;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.query.Field;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.Sort;
import com.codejune.jdbc.query.filter.Compare;
import java.util.*;
import java.util.function.Function;

/**
 * 查询
 *
 * @author ZJ
 * */
public class Query implements Builder {

    private Integer page;

    private Integer size;

    private Filter filter;

    private List<Sort> sort;

    private List<Field> field;

    public Integer getPage() {
        return page;
    }

    public Query setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public Query setSize(Integer size) {
        this.size = size;
        return this;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new Filter();
        }
        return filter;
    }

    public Query setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public List<Sort> getSort() {
        if (sort == null) {
            sort = new ArrayList<>();
        }
        return sort;
    }

    public Query setSort(List<Sort> sort) {
        this.sort = sort;
        return this;
    }

    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<>();
        }
        return field;
    }

    public Query setField(List<Field> field) {
        this.field = field;
        return this;
    }

    /**
     * 是否分页
     *
     * @return 分页返回true
     * */
    public boolean isPaging() {
        return page != null && size != null && page > 0 && size > 0;
    }

    /**
     * 添加sort
     *
     * @param sort sort
     *
     * @return this
     * */
    public Query addSort(Sort sort) {
        if (sort != null) {
            this.getSort().add(sort);
        }
        return this;
    }

    /**
     * 是否排序
     *
     * @return 是否排序
     * */
    public boolean isSort() {
        return !ObjectUtil.isEmpty(sort);
    }

    /**
     * key处理
     *
     * @param action action
     *
     * @return this
     * */
    public Query keyHandler(Function<String, String> action) {
        if (action == null) {
            return this;
        }
        getFilter().getConfig().setCleanNullExclude(ArrayUtil.parse(getFilter().getConfig().getCleanNullExclude(), action));
        getFilter().compareHandler(item -> {
            item.setKey(action.apply(item.getKey()));
            return item;
        });
        for (Sort item : getSort()) {
            item.keyHandler(action);
        }
        return this;
    }

    /**
     * 添加field
     *
     * @param field field
     *
     * @return this
     * */
    public Query addField(Field field) {
        if (field != null) {
            this.getField().add(field);
        }
        return this;
    }

    @Override
    public void build(Object object) {
        Map<String, Object> map = MapUtil.parse(object, String.class, Object.class);
        Object sort = MapUtil.get(map, "sort", Object.class);
        if (sort instanceof Map<?,?> sortMap) {
            List<Sort> list = new ArrayList<>();
            for (Object key : sortMap.keySet()) {
                Sort sortItem = new Sort();
                sortItem.setField(key);
                sortItem.setOrderBy(MapUtil.get(sortMap, Data.toString(key), Sort.OderBy.class));
                list.add(sortItem);
            }
            map.put("sort", list);
        }
        if (map != null) {
            map.put("sort", ArrayUtil.parse(MapUtil.get(map, "sort", List.class), Sort.class));
        }
        Object field = MapUtil.get(map, "field", Object.class);
        if (field instanceof Map<?,?> fieldMap) {
            List<Field> list = new ArrayList<>();
            for (Object key : fieldMap.keySet()) {
                Field fieldItem = new Field();
                fieldItem.setName(ObjectUtil.toString(key));
                fieldItem.setAlias(MapUtil.get(fieldMap, Data.toString(key), String.class));
                list.add(fieldItem);
            }
            map.put("field", list);
        }
        List<?> fieldList = MapUtil.get(map, "field", List.class);
        if (!ObjectUtil.isEmpty(fieldList)) {
            List<Field> newFieldList = new ArrayList<>();
            for (Object item : fieldList) {
                if (item == null) {
                    continue;
                }
                String name;
                String alias = null;
                if (item instanceof String itemString) {
                    name = itemString;
                } else {
                    Map<?, ?> itemMap = MapUtil.parse(item);
                    name = MapUtil.get(itemMap, "name", String.class);
                    alias = MapUtil.get(itemMap, "alias", String.class);
                }
                newFieldList.add(new Field().setName(name).setAlias(alias));
            }
            map.put("field", newFieldList);
        }
        this.setPage(MapUtil.get(map, "page", Integer.class));
        this.setSize(MapUtil.get(map, "size", Integer.class));
        this.setFilter(MapUtil.get(map, "filter", Filter.class));
        this.setSort(ArrayUtil.parse(MapUtil.get(map, "sort", List.class), Sort.class));
        this.setField(ArrayUtil.parse(MapUtil.get(map, "field", List.class), Field.class));
    }

    /**
     * and
     *
     * @param compare compare
     *
     * @return Query
     * */
    public static Query and(Compare ...compare) {
        Filter filter = new Filter();
        for (Compare item : compare) {
            filter.and(item);
        }
        return new Query().setFilter(filter);
    }

}