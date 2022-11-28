package com.codejune.jdbc.mysql;

import com.codejune.common.util.ArrayUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import com.codejune.jdbc.util.SqlBuilder;
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
        return tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        return mysqlJdbc.oracleJdbc.getTable(tableName).insert(data);
    }

    @Override
    public long delete(Filter filter) {
        return mysqlJdbc.oracleJdbc.getTable(tableName).delete(filter);
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return mysqlJdbc.oracleJdbc.getTable(tableName).update(setData, filter);
    }

    @Override
    public long count(Filter filter) {
        return Long.parseLong(mysqlJdbc.oracleJdbc.queryBySql(
                new SqlBuilder(tableName, MysqlJdbc.class).parseCountSql(filter)
        ).get(0).get("C").toString());
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return mysqlJdbc.oracleJdbc.queryBySql(
                new SqlBuilder(tableName, MysqlJdbc.class).parseQueryDataSql(query),
                ArrayUtil.parse("R")
        );
    }

    @Override
    public List<Column> getColumns() {
        return mysqlJdbc.oracleJdbc.getTable(tableName).getColumns();
    }

    @Override
    public String getRemark() {
        return mysqlJdbc.oracleJdbc.getTable(tableName).getRemark();
    }

    @Override
    public void rename(String newTableName) {
        mysqlJdbc.oracleJdbc.getTable(tableName).rename(newTableName);
    }

}