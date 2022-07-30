package com.codejune.common.model;

import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.KeyHandler;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
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
     * @param keyHandler keyHandler
     *
     * @return QueryResult
     * */
    public <E> QueryResult<E> parse(Class<E> eClass, KeyHandler keyHandler) {
        if (eClass == null) {
            throw new InfoException("eClass不能为空");
        }
        if (keyHandler == null) {
            keyHandler = new KeyHandler() {
                @Override
                public Object getNewKey(Object key) {
                    return key;
                }
            };
        }
        QueryResult<E> result = new QueryResult<>();
        result.setCount(this.count);
        List<E> data = new ArrayList<>();
        for (T t : this.data) {
            Map<String, Object> parse = ObjectUtil.parseMap(t, String.class, Object.class);
            Map<?, ?> map = MapUtil.transformKey(parse, keyHandler);
            data.add(MapUtil.parse(map, eClass));
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