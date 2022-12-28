package com.codejune.jdbc;

import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.DataHandler;
import com.codejune.common.util.MapUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * 转换成实体结果
     *
     * @param eClass eClass
     * @param <E> E
     * @param dataHandler dataHandler
     *
     * @return QueryResult
     * */
    public <E> QueryResult<E> parse(Class<E> eClass, DataHandler<Object, Object> dataHandler) {
        if (eClass == null) {
            throw new InfoException("eClass不能为空");
        }
        if (dataHandler == null) {
            dataHandler = key -> key;
        }
        QueryResult<E> result = new QueryResult<>();
        result.setCount(this.count);
        List<E> data = new ArrayList<>();
        for (T t : this.data) {
            Map<String, Object> parse = MapUtil.parse(t, String.class, Object.class);
            Map<?, ?> map = MapUtil.transformKey(parse, dataHandler);
            data.add(MapUtil.transform(map, eClass));
        }
        result.setData(data);
        return result;
    }

    /**
     * 转换成实体结果
     *
     * @param eClass eClass
     * @param <E> E
     *
     * @return QueryResult
     * */
    public <E> QueryResult<E> parse(Class<E> eClass) {
        return parse(eClass, null);
    }

}