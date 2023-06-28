package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class POService<T extends BasePO<ID>, ID> {

    /**
     * 获取数据库
     *
     * @return Database
     * */
    public abstract Database getDatabase();

    /**
     * 获取POClass
     *
     * @return POClass
     * */
    public abstract Class<T> getPOClass();

    /**
     * 查询
     *
     * @param query query
     *
     * @return QueryResult
     * */
    public QueryResult<T> query(Query query) {
        return getTable().query(this.queryHandler(query));
    }

    /**
     * 查询
     *
     * @return QueryResult
     * */
    public final QueryResult<T> query() {
        return query(null);
    }

    /**
     * 查询
     *
     * @param query query
     *
     * @return List
     * */
    public final List<T> queryData(Query query) {
        return getTable().queryData(this.queryHandler(query));
    }

    /**
     * 查询
     *
     * @return QueryResult
     * */
    public final List<T> queryData() {
        return queryData(null);
    }

    /**
     * 通过id查询
     *
     * @param id id
     *
     * @return T
     * */
    public T queryById(ID id) {
        if (id == null) {
            throw new InfoException("id not found");
        }
        QueryResult<T> query = query(new Query().setFilter(new Filter().and(Compare.equals(BasePO.getIdField().getName(), id))));
        if (query.getCount() == 0) {
            throw new InfoException("id not found");
        }
        return query.getData().get(0);
    }

    /**
     * 获取详情
     *
     * @param id id
     *
     * @return Object
     */
    public Object getDetails(ID id) {
        return queryById(id);
    }

    /**
     * query处理
     *
     * @param query query
     *
     * @return Query
     * */
    public Query queryHandler(Query query) {
        return query;
    }

    /**
     * 保存
     *
     * @param t t
     *
     * @return T
     * */
    public T save(T t) {
        beforeSave(t);
        T result = getTable().save(t);
        this.afterSave(result);
        return result;
    }

    /**
     * 保存
     *
     * @param tList tList
     * @param filter 更新条件
     *
     * @return List
     * */
    public final List<T> save(List<T> tList, Filter filter) {
        if (tList == null) {
            tList = new ArrayList<>();
        }
        for (T t : tList) {
            beforeSave(t);
        }
        this.deleteBySave(tList, filter);
        List<T> result = getTable().save(tList);
        for (T t : result) {
            afterSave(t);
        }
        return result;
    }

    /**
     * 保存前操作
     *
     * @param t t
     * */
    public void beforeSave(T t) {
        if (t == null) {
            throw new InfoException("参数缺失");
        }
        for (Field field : BasePO.getColumnFields(getPOClass())) {
            Column column = field.getAnnotation(Column.class);
            String fieldName = field.getName();
            Object o;
            try {
                field.setAccessible(true);
                o = field.get(t);
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
            boolean nullable = column.nullable();
            if (!nullable) {
                if (ObjectUtil.isEmpty(o)) {
                    throw new InfoException(fieldName + "必填");
                }
            }
            int length = column.length();
            if (length > 0) {
                String s = ObjectUtil.toString(o);
                if (!StringUtil.isEmpty(s) && s.length() > length) {
                    throw new InfoException(fieldName + "超长，最大为" + length);
                }
            }
            boolean unique = column.unique();
            if (unique && !ObjectUtil.isEmpty(o)) {
                QueryResult<T> query = query(new Query().setFilter(new Filter().and(Compare.equals(fieldName, o))));
                if (query.getCount() != 0) {
                    String id = ObjectUtil.toString(query.getData().get(0).getId());
                    if (StringUtil.isEmpty(id)) {
                        throw new InfoException("检查数据库数据存在id为空");
                    }
                    if (!id.equals(ObjectUtil.toString(t.getId()))) {
                        throw new InfoException(fieldName + "不能重复");
                    }
                }
            }
        }
    }

    /**
     * 保存后操作
     *
     * @param t t
     * */
    public void afterSave(T t) {}

    /**
     * 删除
     *
     * @param id id
     * */
    public void delete(ID id) {
        getTable().delete(id);
    }

    /**
     * 删除
     *
     * @param t t
     * */
    public final void delete(T t) {
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
    public final void delete(List<T> tList) {
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
    public final void delete() {
        delete(query().getData());
    }

    /**
     * 删除不需要保存的数据
     *
     * @param tList tList
     * @param filter filter
     * */
    public final void deleteBySave(List<T> tList, Filter filter) {
        if (tList == null) {
            tList = new ArrayList<>();
        }
        if (filter == null) {
            filter = new Filter();
        }
        filter.and(Compare.notIn("id", ArrayUtil.parse(tList, t -> {
            if (t == null) {
                return null;
            }
            return t.getId();
        })));
        delete(query(new Query().setFilter(filter)).getData());
    }

    private Database.Table<T, ID> getTable() {
        Database database = getDatabase();
        if (database == null) {
            throw new InfoException("database is null");
        }
        Class<T> poClass = getPOClass();
        if (poClass == null) {
            throw new InfoException("poClass is null");
        }
        return database.getTable(poClass);
    }

}