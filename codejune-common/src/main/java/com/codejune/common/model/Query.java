package com.codejune.common.model;

import com.codejune.common.ModelAble;
import com.codejune.common.handler.KeyHandler;
import com.codejune.common.util.ObjectUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 查询
 *
 * @author ZJ
 * */
public class Query implements ModelAble<Query> {

    private Integer page;

    private Integer size;

    private Filter filter;

    private List<Sort> sort;

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

    public Filter filter() {
        if (filter == null) {
            filter = new Filter();
        }
        return filter;
    }

    public Query setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public List<Sort> sort() {
        if (sort == null) {
            sort = new ArrayList<>();
        }
        return sort;
    }

    public Query setSort(List<Sort> sort) {
        this.sort = sort;
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
            this.sort().add(sort);
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
     * 设置key处理
     *
     * @return this
     * */
    public Query setKeyHandler(KeyHandler keyHandler) {
        if (keyHandler == null) {
            return this;
        }
        if (this.filter != null) {
            this.filter.setKey(keyHandler);
        }
        if (this.sort != null) {
            for (Sort item : sort) {
                item.setKey(keyHandler);
            }
        }
        return this;
    }

    @Override
    public Query assignment(Object object) {
        ObjectUtil.assignment(this, ObjectUtil.parse(object, Map.class));
        return this;
    }

    /**
     * 转换
     *
     * @param object object
     *
     * @return Query
     * */
    public static Query parse(Object object) {
        return new Query().assignment(object);
    }

}