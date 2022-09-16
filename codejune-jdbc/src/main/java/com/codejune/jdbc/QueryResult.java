package com.codejune.jdbc;

import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.ObjectHandler;
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
     * @param objectHandler objectHandler
     *
     * @return QueryResult
     * */
    public <E> QueryResult<E> parse(Class<E> eClass, ObjectHandler objectHandler) {
        if (eClass == null) {
            throw new InfoException("eClass不能为空");
        }
        if (objectHandler == null) {
            objectHandler = new ObjectHandler() {
                @Override
                public Object getNewObject(Object key) {
                    return key;
                }
            };
        }
        QueryResult<E> result = new QueryResult<>();
        result.setCount(this.count);
        List<E> data = new ArrayList<>();
        for (T t : this.data) {
            Map<String, Object> parse = MapUtil.parse(t, String.class, Object.class);
            Map<?, ?> map = MapUtil.transformKey(parse, objectHandler);
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