package com.codejune.service;

import com.codejune.Jdbc;
import com.codejune.common.BaseException;
import com.codejune.Pool;
import com.codejune.common.Buffer;
import com.codejune.common.Closeable;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.service.handler.ColumnToFieldHandler;
import com.codejune.service.handler.FieldToColumnHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 基础数据库
 *
 * @author ZJ
 * */
public class Database {

    private final Function<?, Object> getJdbc;

    private final Consumer<Object> closeJdbc;

    private final String databaseName;

    private final Buffer<Class<?>, ColumnToFieldHandler> columnToFieldHandlerBuffer = new Buffer<>() {
        @Override
        public ColumnToFieldHandler set(Class<?> aClass) {
            return new ColumnToFieldHandler(aClass);
        }
    };

    private final Buffer<Class<?>, FieldToColumnHandler> fieldToColumnHandlerBuffer = new Buffer<>() {
        @Override
        public FieldToColumnHandler set(Class<?> aClass) {
            return new FieldToColumnHandler(aClass);
        }
    };

    public Database(Function<?, Jdbc> getJdbc, String databaseName) {
        this.getJdbc = (Function<Object, Object>) o -> getJdbc.apply(null);
        this.closeJdbc = o -> {
            if (o instanceof Jdbc jdbc) {
                Closeable.closeNoError(jdbc);
            }
        };
        this.databaseName = databaseName;
    }

    public Database(Function<?, Jdbc> getJdbc) {
        this(getJdbc, null);
    }

    @SuppressWarnings("unchecked")
    public Database(Pool<Jdbc> pool, String databaseName) {
        this.getJdbc = (Function<Object, Object>) o -> pool.get();
        this.closeJdbc = o -> {
            if (o instanceof Pool.Source<?> source) {
                pool.returnObject((Pool.Source<Jdbc>) source);
            }
        };
        this.databaseName = databaseName;
    }

    public Database(Pool<Jdbc> pool) {
        this(pool, null);
    }

    /**
     * 获取表
     *
     * @param basePOClass basePOClass
     * @param <T> 泛型
     *
     * @return Table
     * */
    public final <T extends BasePO<ID>, ID> Table<T, ID> getTable(Class<T> basePOClass) {
        return new Table<>(this, basePOClass);
    }

    /**
     * 保存后查询
     *
     * @param jdbc jdbc
     * @param table table
     * @param id id
     * @param <T> T
     * @param <ID> id
     *
     * @return T
     * */
    public <T extends BasePO<ID>, ID> T saveQuery(Jdbc jdbc, Table<T, ID> table, ID id) {
        List<T> data = table.query(Query.and(Compare.equals(BasePO.getIdField().getName(), id))).getData();
        if (ObjectUtil.isEmpty(data)) {
            return null;
        }
        if (data.size() != 1) {
            throw new BaseException("查询出错");
        }
        return data.getFirst();
    }

    /**
     * 获取下一个id的值
     *
     * @param jdbc jdbc
     * @param table table
     * @param <T> 泛型
     * @param <ID> ID
     *
     * @return 下一个id的值
     * */
    public <T extends BasePO<ID>, ID> ID getNextId(Jdbc jdbc, Table<T, ID> table) {
        return null;
    }

    /**
     * 表
     *
     * @author ZJ
     * */
    public static final class Table<T extends BasePO<ID>, ID> {

        private final Database database;

        private final Class<T> basePOClass;

        private final String tableName;

        private Table(Database database, Class<T> basePOClass) {
            this.database = database;
            this.basePOClass = basePOClass;
            this.tableName = BasePO.getTableName(basePOClass);
        }

        public String getTableName() {
            return tableName;
        }

        public Class<T> getBasePOClass() {
            return basePOClass;
        }

        /**
         * 查询
         *
         * @param query query
         *
         * @return QueryResult
         * */
        public QueryResult<T> query(Query query) {
            return execute(jdbc -> {
                if (query != null) {
                    FieldToColumnHandler fieldToColumnHandler = database.fieldToColumnHandlerBuffer.get(basePOClass);
                    query.keyHandler(fieldToColumnHandler::handler);
                }
                ColumnToFieldHandler columnToFieldHandler = database.columnToFieldHandlerBuffer.get(basePOClass);
                return getTable(jdbc).query(query).parse(map -> MapUtil.keyHandler(map, columnToFieldHandler::handler)).parse(basePOClass);
            });
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
         * count
         *
         * @param filter filter
         *
         * @return count
         * */
        public long count(Filter filter) {
            return execute(jdbc -> {
                if (filter != null) {
                    FieldToColumnHandler fieldToColumnHandler = database.fieldToColumnHandlerBuffer.get(basePOClass);
                    filter.compareHandler(compare -> {
                        compare.setKey(fieldToColumnHandler.handler(compare.getKey()));
                        return compare;
                    });
                }
                return getTable(jdbc).count(filter);
            });
        }

        /**
         * count
         *
         * @return count
         * */
        public long count() {
            return count(null);
        }

        /**
         * 保存
         *
         * @param t t
         *
         * @return T
         * */
        public T save(T t) {
            return execute(jdbc -> {
                T saveT;
                if (t == null) {
                    saveT = ObjectUtil.newInstance(basePOClass);
                } else {
                    saveT = t;
                }
                boolean id = saveT.getId() != null;
                if (!id) {
                    saveT.setId(database.getNextId(jdbc, Table.this));
                }
                FieldToColumnHandler fieldToColumnHandler = database.fieldToColumnHandlerBuffer.get(basePOClass);
                Map<String, Object> data = MapUtil.keyHandler(MapUtil.parse(saveT, String.class, Object.class), fieldToColumnHandler::handler);
                if (id) {
                    getTable(jdbc).update(data, new Filter().and(Compare.equals("ID", saveT.getId())));
                } else {
                    getTable(jdbc).insert(data);
                }
                return database.saveQuery(jdbc, Table.this, saveT.getId());
            });
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
                result.add(this.save(t));
            }
            return result;
        }

        /**
         * 删除
         *
         * @param id id
         * */
        public void delete(ID id) {
            if (id == null) {
                return;
            }
            execute(jdbc -> {
                getTable(jdbc).delete(new Filter().and(Compare.equals("ID", id)));
                return null;
            });
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

        /**
         * 删除
         * */
        public void delete() {
            execute(jdbc -> {
                getTable(jdbc).delete();
                return null;
            });
        }

        private <RESULT> RESULT execute(Function<Jdbc, RESULT> function) {
            Object object = database.getJdbc.apply(null);
            try {
                if (object instanceof Jdbc jdbc) {
                    return function.apply(jdbc);
                } else if (object instanceof Pool.Source<?> source) {
                    return function.apply((Jdbc) source.getSource());
                } else {
                    throw new BaseException("database.getJdbc not analysis");
                }
            } finally {
                database.closeJdbc.accept(object);
            }
        }

        private com.codejune.jdbc.Table getTable(Jdbc jdbc) {
            com.codejune.jdbc.Database jdbcDatabase;
            if (StringUtil.isEmpty(database.databaseName)) {
                jdbcDatabase = jdbc.getDefaultDatabase();
            } else {
                jdbcDatabase = jdbc.getDatabase(database.databaseName);
            }
            return jdbcDatabase.getTable(tableName);
        }

    }

}