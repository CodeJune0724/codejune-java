package com.codejune.service;

import com.codejune.common.ClassInfo;
import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.query.Filter;
import java.lang.reflect.Field;
import java.util.List;

public final class BasePOService<T extends BasePO<ID>, ID> implements POService<T, ID> {

    private final Database database;

    public BasePOService(Database database) {
        if (database == null) {
            throw new InfoException("database is null");
        }
        this.database = database;
    }

    @Override
    public QueryResult<T> query(Query query) {
        return database.getTable(getPOClass()).query(query);
    }

    @Override
    public T save(T t) {
        if (t == null) {
            throw new InfoException("参数缺失");
        }
        List<Field> columnFields = BasePO.getColumnFields(getPOClass());
        for (Field field : columnFields) {
            Column column = field.getAnnotation(Column.class);
            String fieldName = field.getName();
            Object o;
            try {
                field.setAccessible(true);
                o = field.get(t);
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }

            // 必填校验
            boolean required = column.required();
            if (required) {
                if (ObjectUtil.isEmpty(o)) {
                    throw new InfoException(fieldName + "必填");
                }
            }

            // 长度校验
            int length = column.length();
            if (length > 0) {
                String s = ObjectUtil.toString(o);
                if (!StringUtil.isEmpty(s) && s.length() > length) {
                    throw new InfoException(fieldName + "超长，最大为" + length);
                }
            }

            // 唯一校验
            boolean unique = column.unique();
            if (unique) {
                QueryResult<T> query = query(new Query().setFilter(new Filter().and(Filter.Item.equals(fieldName, o))));
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
        T saveEntity = database.getTable(getPOClass()).save(t);
        if (saveEntity == null) {
            return null;
        }
        return query(new Query().setFilter(new Filter().and(Filter.Item.equals(BasePO.getIdName(), saveEntity.getId())))).getData().get(0);
    }

    @Override
    public void delete(ID id) {
        database.getTable(getPOClass()).delete(id);
    }

    @Override
    public void delete() {
        database.getTable(getPOClass()).delete();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getPOClass() {
        ClassInfo classInfo = new ClassInfo(this.getClass());
        ClassInfo superClass = classInfo.getSuperClass(BasePOService.class);
        if (superClass == null) {
            throw new InfoException("类错误");
        }
        return (Class<T>) superClass.getGenericClass().get(0).getOriginClass();
    }

}