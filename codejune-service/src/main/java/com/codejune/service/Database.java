package com.codejune.service;

import com.codejune.Jdbc;
import com.codejune.common.BaseException;
import com.codejune.common.Pool;
import com.codejune.common.util.ArrayUtil;
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
import java.util.function.Function;

/**
 * 基础数据库
 *
 * @author ZJ
 * */
public abstract class Database {

    private final Pool<Jdbc> pool;

    private final String databaseName;

    private final Function<?, Jdbc> getJdbcFunction;

    public Database(Pool<Jdbc> pool, String databaseName) {
        this.pool = pool;
        this.databaseName = databaseName;
        this.getJdbcFunction = null;
    }

    public Database(Pool<Jdbc> pool) {
        this(pool, null);
    }

    public Database(Function<?, Jdbc> getJdbcFunction, String databaseName) {
        this.pool = null;
        this.databaseName = databaseName;
        this.getJdbcFunction = getJdbcFunction;
    }

    public Database(Function<?, Jdbc> getJdbcFunction) {
        this(getJdbcFunction, null);
    }

    /**
     * 获取表
     *
     * @param <T> 泛型
     * @param basePOClass basePOClass
     *
     * @return Table
     * */
    public synchronized final <T extends BasePO<ID>, ID> Table<T, ID> getTable(Class<T> basePOClass) {
        Connection connection;
        if (pool != null) {
            Pool.Source<Jdbc> jdbcSource = pool.get();
            connection = new Connection(jdbcSource.getSource()) {
                @Override
                public void close() {
                    pool.returnObject(jdbcSource);
                }
            };
        } else if (this.getJdbcFunction != null) {
            Jdbc then = this.getJdbcFunction.apply(null);
            connection = new Connection(then) {
                @Override
                public void close() {
                    then.close();
                }
            };
        } else {
            throw new BaseException("数据库异常");
        }
        try {
            return new Table<>(this, connection, basePOClass);
        } catch (Exception e) {
            connection.close();
            throw new BaseException(e);
        }
    }

    /**
     * 保存后查询
     *
     * @param jdbc jdbc
     * @param tableName 表名
     * @param id id
     * @param tClass tClass
     * @param <T> T
     * @param <ID> id
     *
     * @return T
     * */
    public <T extends BasePO<ID>, ID> T saveQuery(Jdbc jdbc, String tableName, ID id, Class<T> tClass) {
        List<Map<String, Object>> list = jdbc.getDefaultDatabase().getTable(tableName).queryData(new Query().setFilter(new Filter().and(Compare.equals(BasePO.getIdField().getName(), id))));
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() != 1) {
            throw new Error("查询出错");
        }
        Map<String, Object> map = list.get(0);
        ColumnToFieldHandler columnToFieldHandler = new ColumnToFieldHandler(tClass);
        return ObjectUtil.transform(MapUtil.keyHandler(map, columnToFieldHandler::handler), tClass);
    }

    /**
     * 获取下一个id的值
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

        private final Connection connection;

        private final Class<T> basePOClass;

        private final FieldToColumnHandler fieldToColumnHandler;

        private final ColumnToFieldHandler columnToFieldHandler;

        private final String tableName;

        private Table(Database database, Connection connection, Class<T> basePOClass) {
            this.database = database;
            this.connection = connection;
            this.basePOClass = basePOClass;
            this.fieldToColumnHandler = new FieldToColumnHandler(basePOClass);
            this.columnToFieldHandler = new ColumnToFieldHandler(basePOClass);
            this.tableName = BasePO.getTableName(basePOClass);
        }

        /**
         * 获取表名
         *
         * @return 表名
         * */
        public String getTableName() {
            return this.tableName;
        }

        /**
         * 获取basePOClass
         *
         * @return basePOClass
         * */
        public Class<T> getBasePOClass() {
            return this.basePOClass;
        }

        /**
         * 查询
         *
         * @param query query
         *
         * @return QueryResult
         * */
        public QueryResult<T> query(Query query) {
            try {
                if (query == null) {
                    query = new Query();
                }
                query.keyHandler(fieldToColumnHandler::handler);
                return getTable().query(query).parse(map -> MapUtil.keyHandler(map, columnToFieldHandler::handler)).parse(basePOClass);
            } finally {
                this.connection.close();
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
         * 查询数据
         *
         * @param query query
         *
         * @return List
         * */
        public List<T> queryData(Query query) {
            try {
                if (query == null) {
                    query = new Query();
                }
                return ArrayUtil.parse(getTable().queryData(query.keyHandler(fieldToColumnHandler::handler)), map -> MapUtil.transform(MapUtil.keyHandler(map, columnToFieldHandler::handler), basePOClass));
            } finally {
                this.connection.close();
            }
        }

        /**
         * 查询数据
         *
         * @return List
         * */
        public List<T> queryData() {
            return queryData(null);
        }

        /**
         * 保存
         *
         * @param t t
         * @param close 是否关闭连接
         *
         * @return T
         * */
        private T save(T t, boolean close) {
            try {
                if (t == null) {
                    t = this.basePOClass.getConstructor().newInstance();
                }
                com.codejune.jdbc.Table table = getTable();
                boolean isId = t.getId() != null;
                if (!isId) {
                    t.setId(database.getNextId(this.connection.getJdbc(), this));
                }
                Map<String, Object> map = MapUtil.keyHandler(MapUtil.parse(t, String.class, Object.class), fieldToColumnHandler::handler);
                if (isId) {
                    table.update(map, new Filter().and(Compare.equals("ID", t.getId())));
                } else {
                    table.insert(map);
                }
                return this.database.saveQuery(this.connection.getJdbc(), this.getTableName(), t.getId(), this.getBasePOClass());
            } catch (Exception e) {
                throw new BaseException(e);
            } finally {
                if (close) {
                    this.connection.close();
                }
            }
        }

        /**
         * 保存
         *
         * @param t t
         *
         * @return T
         * */
        public T save(T t) {
            return this.save(t, true);
        }

        /**
         * 保存
         *
         * @param tList tList
         *
         * @return List
         * */
        public List<T> save(List<T> tList) {
            try {
                ArrayList<T> result = new ArrayList<>();
                if (ObjectUtil.isEmpty(tList)) {
                    return result;
                }
                for (T t : tList) {
                    result.add(this.save(t, false));
                }
                return result;
            } finally {
                this.connection.close();
            }
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
            try {
                getTable().delete(new Filter().and(Compare.equals("ID", id)));
            } finally {
                this.connection.close();
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

        /**
         * 删除
         * */
        public void delete() {
            try {
                getTable().delete();
            } finally {
                this.connection.close();
            }
        }

        private com.codejune.jdbc.Table getTable() {
            com.codejune.jdbc.Database jdbcDatabase;
            if (StringUtil.isEmpty(this.database.databaseName)) {
                jdbcDatabase = this.connection.getJdbc().getDefaultDatabase();
            } else {
                jdbcDatabase = this.connection.getJdbc().getDatabase(this.database.databaseName);
            }
            return jdbcDatabase.getTable(tableName);
        }

    }

    /**
     * 单个连接资源
     *
     * @author ZJ
     * */
    private static abstract class Connection {

        private final Jdbc jdbc;

        public Connection(Jdbc source) {
            this.jdbc = source;
        }

        public Jdbc getJdbc() {
            return jdbc;
        }

        public abstract void close();

    }

}