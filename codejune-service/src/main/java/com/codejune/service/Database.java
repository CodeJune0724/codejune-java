package com.codejune.service;

import com.codejune.Jdbc;
import com.codejune.core.BaseException;
import com.codejune.Pool;
import com.codejune.core.Buffer;
import com.codejune.core.Closeable;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.service.handler.ColumnToFieldHandler;
import com.codejune.service.handler.FieldToColumnHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基础数据库
 *
 * @author ZJ
 * */
public class Database {

    private final Supplier<Jdbc> getJdbc;

    private final Consumer<Jdbc> closeJdbc;

    private final String databaseName;

    private final Buffer<Class<?>, ColumnToFieldHandler> columnToFieldHandlerBuffer = new Buffer<>() {
        @Override
        public ColumnToFieldHandler generateValue(Class<?> aClass) {
            return new ColumnToFieldHandler(aClass);
        }
    };

    private final Buffer<Class<?>, FieldToColumnHandler> fieldToColumnHandlerBuffer = new Buffer<>() {
        @Override
        public FieldToColumnHandler generateValue(Class<?> aClass) {
            return new FieldToColumnHandler(aClass);
        }
    };

    public Database(Supplier<Jdbc> getJdbc, String databaseName) {
        this.getJdbc = getJdbc;
        this.closeJdbc = Closeable::closeNoError;
        this.databaseName = databaseName;
    }

    public Database(Supplier<Jdbc> getJdbc) {
        this(getJdbc, null);
    }

    public Database(Pool<Jdbc> pool, String databaseName) {
        this.getJdbc = pool::get;
        this.closeJdbc = pool::returnObject;
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
     * 执行
     *
     * @param function function
     * */
    public void execute(Consumer<Jdbc> function) {
        execute(jdbc -> {
            function.accept(jdbc);
            return null;
        });
    }

    private <RESULT> RESULT execute(Function<Jdbc, RESULT> function) {
        Jdbc jdbc = this.getJdbc.get();
        try {
            return function.apply(jdbc);
        } finally {
            closeJdbc.accept(jdbc);
        }
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
            return database.execute(jdbc -> {
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
            return database.execute(jdbc -> {
                if (filter != null) {
                    FieldToColumnHandler fieldToColumnHandler = database.fieldToColumnHandlerBuffer.get(basePOClass);
                    filter.expressionHandler(compare -> {
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
            return database.execute(jdbc -> {
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
            database.execute(jdbc -> {
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
            database.execute(jdbc -> {
                getTable(jdbc).delete();
                return null;
            });
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