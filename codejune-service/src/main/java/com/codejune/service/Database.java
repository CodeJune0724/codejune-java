package com.codejune.service;

import com.codejune.Jdbc;
import com.codejune.common.Pool;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.model.Filter;
import com.codejune.common.model.Query;
import com.codejune.common.model.QueryResult;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.service.handler.ColumnToFieldKeyHandler;
import com.codejune.service.handler.FieldToColumnKeyHandler;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基础数据库
 *
 * @author ZJ
 * */
public abstract class Database {

    private final Pool<Jdbc> pool;

    private final String databaseName;

    public Database(Pool<Jdbc> pool, String databaseName) {
        this.pool = pool;
        this.databaseName = databaseName;
    }

    public Database(Pool<Jdbc> pool) {
        this(pool, null);
    }

    /**
     * 切换表
     *
     * @param <T> 泛型
     * @param basePOClass basePOClass
     *
     * @return Table
     * */
    public final <T extends BasePO> Table<T> switchTable(Class<T> basePOClass) {
        return new Table<>(this, basePOClass);
    }

    /**
     * 获取下一个id的值
     *
     * @param <T> 泛型
     * @param table table
     *
     * @return 下一个id的值
     * */
    public <T extends BasePO> Object getNextId(Table<T> table) {
        return null;
    }

    /**
     * 获取下一个id的值
     *
     * @return 下一个id的值
     * */
    public Object getNextId() {
        return null;
    }

    private <T extends BasePO> Object actualGetNextId(Table<T> table) {
        Object nextId = getNextId(table);
        if (nextId != null) {
            return nextId;
        }
        nextId = getNextId();
        return nextId;
    }

    /**
     * 表
     *
     * @author ZJ
     * */
    public static final class Table<T extends BasePO> {

        private final Database database;

        private final Class<T> basePOClass;

        private final FieldToColumnKeyHandler fieldToColumnKeyHandler;

        private final ColumnToFieldKeyHandler columnToFieldKeyHandler;

        private Table(Database database, Class<T> basePOClass) {
            this.database = database;
            this.basePOClass = basePOClass;
            this.fieldToColumnKeyHandler = new FieldToColumnKeyHandler(basePOClass);
            this.columnToFieldKeyHandler = new ColumnToFieldKeyHandler(basePOClass);
        }

        /**
         * 查询
         *
         * @param query query
         *
         * @return QueryResult
         * */
        public QueryResult<T> query(Query query) {
            if (query == null) {
                query = new Query();
            }
            query.setKeyHandler(fieldToColumnKeyHandler);
            Jdbc jdbc = this.database.pool.get();
            try {
                return jdbc.getTable(getCompleteTableName()).query(query).parse(basePOClass, columnToFieldKeyHandler);
            } finally {
                this.database.pool.returnObject(jdbc);
            }
        }

        /**
         * 查询
         *
         * @return QueryResult
         * */
        public QueryResult<T> query() {
            return query(null);
        }

        /**
         * 保存
         *
         * @param t t
         *
         * @return T
         * */
        public T save(T t) {
            if (t == null) {
                try {
                    t = this.basePOClass.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new InfoException(e.getMessage());
                }
            }
            Jdbc jdbc = this.database.pool.get();
            try {
                com.codejune.jdbc.Table table = jdbc.getTable(getCompleteTableName());
                List<String> columnList = new ArrayList<>();
                List<Field> allFields = BasePO.getAllFields(basePOClass);
                for (Field field : allFields) {
                    columnList.add(field.getName());
                }
                boolean isId = t.getId() != null;
                if (!isId) {
                    t.setId(database.actualGetNextId(this));
                }
                Map<String, Object> map = MapUtil.filterKey(ObjectUtil.parseMap(t, String.class, Object.class), columnList);
                map = MapUtil.parseToGeneric(MapUtil.transformKey(map, fieldToColumnKeyHandler), String.class, Object.class);
                if (isId) {
                    table.update(new Filter().and(Filter.Item.equals(BasePO.idName(), t.getId())), map);
                } else {
                    table.insert(Collections.singletonList(map));
                }
            } finally {
                this.database.pool.returnObject(jdbc);
            }
            QueryResult<T> query = query(new Query().setFilter(new Filter().and(Filter.Item.equals(BasePO.idField().getName(), t.getId()))));
            if (query.getCount() == 0) {
                return null;
            }
            if (query.getCount() != 1) {
                throw new ErrorException("查询出错");
            }
            return query.getData().get(0);
        }

        /**
         * 保存
         *
         * @param tList tList
         *
         * @return List
         * */
        public List<T> save(List<T> tList) {
            ArrayList<T> result = new ArrayList<>();
            if (ObjectUtil.isEmpty(tList)) {
                return result;
            }
            for (T t : tList) {
                result.add(save(t));
            }
            return result;
        }

        /**
         * 删除
         *
         * @param id id
         * */
        public void delete(Object id) {
            if (id == null) {
                return;
            }
            Jdbc jdbc = this.database.pool.get();
            try {
                com.codejune.jdbc.Table table = jdbc.getTable(getCompleteTableName());
                table.delete(new Filter().and(Filter.Item.equals(BasePO.idName(), id)));
            } finally {
                this.database.pool.returnObject(jdbc);
            }
        }

        /**
         * 删除
         *
         * @param t t
         * */
        public void delete(T t) {
            if (t == null) {
                return;
            }
            if (t.getId() == null) {
                return;
            }
            delete(t.getId());
        }

        private String getCompleteTableName() {
            String result = BasePO.tableName(basePOClass);
            if (!StringUtil.isEmpty(database.databaseName)) {
                result = database.databaseName + "." + result;
            }
            return result;
        }

    }

}