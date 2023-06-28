package com.codejune.jdbc;

import com.codejune.common.Action;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询结果
 *
 * @author ZJ
 * */
public final class QueryResult<T> {

    private Long count = 0L;

    private List<T> data = new ArrayList<>();

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 转换
     *
     * @param eClass eClass
     * @param <E> E
     *
     * @return QueryResult
     * */
    public <E> QueryResult<E> parse(Class<E> eClass) {
        if (eClass == null) {
            throw new InfoException("eClass不能为空");
        }
        QueryResult<E> result = new QueryResult<>();
        result.setCount(this.count);
        List<E> newData = new ArrayList<>();
        for (T t : this.data) {
            newData.add(ObjectUtil.transform(t, eClass));
        }
        result.setData(newData);
        return result;
    }

    /**
     * 转换
     *
     * @param action action
     *
     * @return QueryResult
     * */
    public QueryResult<T> parse(Action<T, T> action) {
        if (action == null) {
            return this;
        }
        QueryResult<T> result = new QueryResult<>();
        result.setCount(this.count);
        List<T> newData = new ArrayList<>();
        for (T t : this.data) {
            T newT = action.then(t);
            if (newT == null) {
                continue;
            }
            newData.add(newT);
        }
        result.setData(newData);
        return result;
    }

}