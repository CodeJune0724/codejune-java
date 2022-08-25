package com.codejune.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table
 *
 * @author ZJ
 * */
public interface Table {

    /**
     * 获取表名
     *
     * @return 表名
     * */
    String getName();

    /**
     * 插入数据
     *
     * @param data 数据
     *
     * @return 受影响的行数
     * */
    long insert(List<Map<String, Object>> data);

    /**
     * 插入数据
     *
     * @param data 数据
     *
     * @return 受影响的行数
     * */
    default long insert(Map<String, Object> data) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (data != null) {
            list.add(data);
        }
        return insert(list);
    }

    /**
     * 删除数据
     *
     * @param filter 数据过滤
     *
     * @return 受影响的行数
     * */
    long delete(Filter filter);

    /**
     * 删除数据
     *
     * @return 受影响的行数
     * */
    default long delete() {
        return delete(null);
    }

    /**
     * 修改数据
     *
     * @param filter 数据过滤
     * @param setData 设置的数据
     *
     * @return 受影响的行数
     * */
    long update(Filter filter, Map<String, Object> setData);

    /**
     * 查询
     *
     * @param query query
     *
     * @return QueryResult
     * */
    QueryResult<Map<String, Object>> query(Query query);

    /**
     * 查询
     *
     * @return QueryResult
     * */
    default QueryResult<Map<String, Object>> query() {
        return query(null);
    }

}