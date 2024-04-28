package com.codejune.jdbc;

import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.query.Filter;
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
     * @param setData 设置的数据
     * @param filter 数据过滤
     *
     * @return 受影响的行数
     * */
    long update(Map<String, Object> setData, Filter filter);

    /**
     * 统计
     *
     * @param filter filter
     *
     * @return 数量
     * */
    long count(Filter filter);

    /**
     * 统计
     *
     * @return 数量
     * */
    default long count() {
        return count(null);
    }

    /**
     * 查询数据
     *
     * @param query query
     *
     * @return List
     * */
    List<Map<String, Object>> queryData(Query query);

    /**
     * 查询数据
     *
     * @return List
     * */
    default List<Map<String, Object>> queryData() {
        return queryData(null);
    }

    /**
     * 查询
     *
     * @param query query
     *
     * @return QueryResult
     * */
    default QueryResult<Map<String, Object>> query(Query query) {
        if (query == null) {
            query = new Query();
        }
        QueryResult<Map<String, Object>> result = new QueryResult<>();
        result.setData(queryData(query));
        if (query.getCount() == null || query.getCount()) {
            result.setCount(count(query.getFilter()));
        } else {
            result.setCount(ObjectUtil.transform(result.getData().size(), Long.class));
        }
        return result;
    }

    /**
     * 查询
     *
     * @return QueryResult
     * */
    default QueryResult<Map<String, Object>> query() {
        return query(null);
    }

}