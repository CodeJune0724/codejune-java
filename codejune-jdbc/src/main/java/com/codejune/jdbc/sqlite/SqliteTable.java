package com.codejune.jdbc.sqlite;

import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import java.util.List;
import java.util.Map;

/**
 * SqliteTable
 *
 * @author ZJ
 * */
public final class SqliteTable extends SqlTable {

    private static final Object LOCK = new Object();

    public SqliteTable(SqliteDatabase sqliteDatabase, String name) {
        super(sqliteDatabase, name);
    }

    @Override
    public SqliteDatabase getDatabase() {
        return (SqliteDatabase) super.getDatabase();
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        synchronized (LOCK) {
            return super.insert(data);
        }
    }

    @Override
    public long delete(Filter filter) {
        synchronized (LOCK) {
            return super.delete(filter);
        }
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        synchronized (LOCK) {
            return super.update(setData, filter);
        }
    }

}