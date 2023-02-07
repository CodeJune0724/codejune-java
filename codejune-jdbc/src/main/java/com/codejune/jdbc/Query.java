package com.codejune.jdbc;

import com.codejune.common.Action;
import com.codejune.common.Builder;
import com.codejune.common.DataType;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.query.Field;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.Sort;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public boolean isPage() {
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
    public Query keyHandler(Action<String, String> action) {
        if (action == null) {
            return this;
        }
        if (this.filter != null) {
            this.filter.itemHandler(item -> {
                item.setKey(action.then(item.getKey()));
                return item;
            });
        }
        if (this.sort != null) {
            for (Sort item : sort) {
                item.keyHandler(action);
            }
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
        Object sort = MapUtil.getValue(map, "sort", Object.class);
        if (sort != null && DataType.parse(sort.getClass()) == DataType.MAP) {
            List<Map<?, ?>> list = new ArrayList<>();
            list.add(MapUtil.parse(sort));
            map.put("sort", list);
        }
        List<?> field = MapUtil.getValue(map, "field", List.class);
        if (!ObjectUtil.isEmpty(field)) {
            List<Field> fieldList = new ArrayList<>();
            for (Object item : field) {
                if (item == null) {
                    continue;
                }
                String name = null;
                String alias = null;
                if (item instanceof String) {
                    name = ObjectUtil.toString(item);
                }
                if (DataType.parse(item.getClass()) == DataType.MAP) {
                    Map<?, ?> itemMap = MapUtil.parse(item);
                    name = MapUtil.getValue(itemMap, "name", String.class);
                    alias = MapUtil.getValue(itemMap, "alias", String.class);
                }
                fieldList.add(new Field().setName(name).setAlias(alias));
            }
            map.put("field", fieldList);
        }
        ObjectUtil.assignment(this, map);
    }

    /**
     * 转换
     *
     * @param object object
     *
     * @return Query
     * */
    public static Query parse(Object object) {
        Query result = new Query();
        result.build(object);
        return result;
    }

}