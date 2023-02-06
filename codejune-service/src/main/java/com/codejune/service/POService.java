package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.query.Filter;
import java.util.ArrayList;
import java.util.List;

public interface POService<T extends BasePO<ID>, ID> {

    /**
     * 查询
     *
     * @param query query
     *
     * @return QueryResult
     * */
    QueryResult<T> query(Query query);

    /**
     * 查询
     *
     * @return QueryResult
     * */
    default QueryResult<T> query() {
        return query(null);
    }

    /**
     * 通过id查询
     *
     * @param id id
     *
     * @return T
     * */
    default T queryById(ID id) {
        if (id == null) {
            throw new InfoException("id not found");
        }
        QueryResult<T> query = query(new Query().setFilter(new Filter().and(Filter.Item.equals(BasePO.getIdField().getName(), id))));
        if (query.getCount() == 0) {
            throw new InfoException("id not found");
        }
        return query.getData().get(0);
    }

    /**
     * 保存
     *
     * @param t t
     *
     * @return T
     * */
    T save(T t);

    /**
     * 保存
     *
     * @param tList t
     *
     * @return List
     * */
    default List<T> save(List<T> tList) {
        List<T> result = new ArrayList<>();
        if (ObjectUtil.isEmpty(tList)) {
            return result;
        }
        for (T t : tList) {
            T save = save(t);
            if (save == null) {
                continue;
            }
            result.add(save);
        }
        return result;
    }

    /**
     * 删除
     *
     * @param id id
     * */
    void delete(ID id);

    /**
     * 删除
     *
     * @param t t
     * */
    default void delete(T t) {
        if (t == null) {
            return;
        }
        delete(t.getId());
    }

    /**
     * 删除
     *
     * @param tList tList
     * */
    default void delete(List<T> tList) {
        if (ObjectUtil.isEmpty(tList)) {
            return;
        }
        for (T t : tList) {
            delete(t);
        }
    }

    /**
     * 删除
     * */
    void delete();

    /**
     * 获取详情
     *
     * @param id id
     *
     * @return Object
     */
    default Object getDetails(ID id) {
        return queryById(id);
    }

}