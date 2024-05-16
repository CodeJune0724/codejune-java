package com.codejune.jdbc;

import com.codejune.common.Builder;
import com.codejune.common.Data;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
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

    private Boolean count;

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

    public Boolean getCount() {
        if (this.count == null) {
            return this.paging();
        }
        return count;
    }

    public Query setCount(Boolean count) {
        this.count = count;
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
    public boolean paging() {
        return page != null && size != null && page > 0 && size > 0;
    }

    /**
     * 添加sort
     *
     * @param field field
     * @param orderBy orderBy
     *
     * @return this
     * */
    public Query addSort(String field, Sort.OderBy orderBy) {
        if (StringUtil.isEmpty(field)) {
            return this;
        }
        if (orderBy == null) {
            orderBy = Sort.OderBy.ASC;
        }
        this.getSort().add(new Sort().setField(field).setOrderBy(orderBy));
        return this;
    }

    /**
     * 添加field
     *
     * @param name name
     * @param alias alias
     *
     * @return this
     * */
    public Query addField(String name, String alias) {
        this.getField().add(new Field().setName(name).setAlias(alias));
        return this;
    }

    /**
     * 添加field
     *
     * @param name name
     *
     * @return this
     * */
    public Query addField(String name) {
        return this.addField(name, null);
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
        getFilter().getConfig().setCleanNullExclude(ArrayUtil.parseList(getFilter().getConfig().getCleanNullExclude(), action));
        getFilter().compareHandler(item -> {
            item.setKey(action.apply(item.getKey()));
            return item;
        });
        for (Sort item : getSort()) {
            item.keyHandler(action);
        }
        for (Field field : getField()) {
            field.keyHandler(action);
        }
        return this;
    }

    @Override
    public void build(Object object) {
        Map<String, Object> map = MapUtil.parse(object, String.class, Object.class);
        if (map == null) {
            return;
        }
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
        map.put("sort", ArrayUtil.parse(MapUtil.get(map, "sort", List.class), Sort.class));
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
        this.setCount(MapUtil.get(map, "count", Boolean.class));
        this.setFilter(MapUtil.get(map, "filter", Filter.class));
        this.setSort(ArrayUtil.parseList(MapUtil.get(map, "sort", List.class), Sort.class));
        this.setField(ArrayUtil.parseList(MapUtil.get(map, "field", List.class), Field.class));
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