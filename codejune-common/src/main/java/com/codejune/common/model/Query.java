package com.codejune.common.model;

import com.codejune.common.ModelAble;
import com.codejune.common.handler.KeyHandler;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
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

    private Sort sort;

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

    public Query setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public Query setSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 获取filter
     *
     * @return Filter
     * */
    public Filter filter() {
        if (filter == null) {
            filter = new Filter();
        }
        return filter;
    }

    /**
     * 获取sort
     *
     * @return Sort
     * */
    public Sort sort() {
        if (sort == null) {
            sort = new Sort();
        }
        return sort;
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
            this.sort.setKey(keyHandler);
        }
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
     * 是否排序
     *
     * @return 是否排序
     * */
    public boolean isSort() {
        return sort != null && !StringUtil.isEmpty(sort.getColumn()) && sort.getOrderBy() != null;
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