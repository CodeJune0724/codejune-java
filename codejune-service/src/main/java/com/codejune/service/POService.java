package com.codejune.service;

import com.codejune.core.BaseException;
import com.codejune.core.ClassInfo;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;
import jakarta.persistence.Column;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class POService<T extends BasePO<ID>, ID> {

    private final Class<T> poClass;

    @SuppressWarnings("unchecked")
    public POService() {
        List<ClassInfo> superClassList = new ClassInfo(this.getClass()).getSuperClass();
        if (ObjectUtil.isEmpty(superClassList)) {
            throw new BaseException("superClass is null");
        }
        ClassInfo superClass = superClassList.getFirst();
        List<ClassInfo> genericClassList = superClass.getGenericClass();
        if (ObjectUtil.isEmpty(genericClassList)) {
            throw new BaseException("genericClass is null");
        }
        this.poClass = (Class<T>) genericClassList.getFirst().getJavaClass();
    }

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
    public final Class<T> getPOClass() {
        return this.poClass;
    }

    /**
     * 查询
     *
     * @param query query
     *
     * @return QueryResult
     * */
    public QueryResult<T> query(Query query) {
        return getTable().query(query);
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
     * count
     *
     * @param filter filter
     *
     * @return count
     * */
    public final long count(Filter filter) {
        return getTable().count(filter);
    }

    /**
     * count
     *
     * @return count
     * */
    public final long count() {
        return getTable().count();
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
            throw new BaseException("id not found");
        }
        QueryResult<T> query = query(Query.and(Compare.equals(BasePO.getIdField().getName(), id)));
        if (query.getCount() == 0) {
            throw new BaseException("id not found");
        }
        return query.getData().getFirst();
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
        if (filter == null) {
            filter = new Filter();
        }
        for (T t : tList) {
            beforeSave(t);
        }
        filter.and(Compare.notIn("id", ArrayUtil.parse(tList, t -> {
            if (t == null) {
                return null;
            }
            return t.getId();
        })));
        delete(query(new Query().setFilter(filter)).getData());
        List<T> result = getTable().save(tList);
        for (T t : result) {
            afterSave(t);
        }
        return result;
    }

    /**
     * 保存
     *
     * @param tList tList
     *
     * @return List
     * */
    public final List<T> save(List<T> tList) {
        return this.save(tList, null);
    }

    /**
     * 保存前操作
     *
     * @param t t
     * */
    public void beforeSave(T t) {
        if (t == null) {
            throw new BaseException("参数缺失");
        }
        for (Field field : BasePO.getColumnFields(poClass)) {
            Column column = field.getAnnotation(Column.class);
            String fieldName = field.getName();
            Object o;
            try {
                field.setAccessible(true);
                o = field.get(t);
            } catch (Exception e) {
                throw new BaseException(e.getMessage());
            }
            boolean nullable = column.nullable();
            if (!nullable) {
                if (ObjectUtil.isEmpty(o)) {
                    throw new BaseException(fieldName + "必填");
                }
            }
            int length = column.length();
            if (length > 0) {
                String s = ObjectUtil.toString(o);
                if (!StringUtil.isEmpty(s) && s.length() > length) {
                    throw new BaseException(fieldName + "超长，最大为" + length);
                }
            }
            boolean unique = column.unique();
            if (unique && !ObjectUtil.isEmpty(o)) {
                QueryResult<T> query = query(Query.and(Compare.equals(fieldName, o)));
                if (query.getCount() != 0) {
                    String id = ObjectUtil.toString(query.getData().getFirst().getId());
                    if (StringUtil.isEmpty(id)) {
                        throw new BaseException("检查数据库数据存在id为空");
                    }
                    if (!id.equals(ObjectUtil.toString(t.getId()))) {
                        throw new BaseException(fieldName + "不能重复");
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
     * 获取表
     *
     * @return Database.Table
     * */
    public final Database.Table<T, ID> getTable() {
        return getDatabase().getTable(this.poClass);
    }

}