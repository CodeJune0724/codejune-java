package com.codejune.jdbc.mysql;

import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import java.util.List;
import java.util.Map;

/**
 * MysqlTable
 *
 * @author ZJ
 * */
public final class MysqlTable implements SqlTable {

    private final MysqlJdbc mysqlJdbc;

    private final String tableName;

    public MysqlTable(MysqlJdbc mysqlJdbc, String tableName) {
        this.mysqlJdbc = mysqlJdbc;
        this.tableName = tableName;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        return 0;
    }

    @Override
    public long delete(Filter filter) {
        return 0;
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return 0;
    }

    @Override
    public long count(Filter filter) {
        return 0;
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return null;
    }

    @Override
    public List<Column> getColumns() {
        return null;
    }

    @Override
    public String getRemark() {
        return null;
    }

}