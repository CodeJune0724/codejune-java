package com.codejune.jdbc.mysql;

import com.codejune.common.util.ArrayUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.oracle.OracleJdbc;
import com.codejune.jdbc.oracle.OracleTable;
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

    private final MysqlDatabase mysqlDatabase;

    private final String tableName;

    private final OracleTable oracleTable;

    MysqlTable(MysqlDatabase mysqlDatabase, String tableName) {
        this.mysqlDatabase = mysqlDatabase;
        this.tableName = tableName;
        this.oracleTable = mysqlDatabase.oracleDatabase.getTable(tableName);
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        return oracleTable.insert(data);
    }

    @Override
    public long delete(Filter filter) {
        return mysqlDatabase.mysqlJdbc.execute(new SqlBuilder(tableName, OracleJdbc.class).parseDeleteSql(filter));
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return oracleTable.update(setData, filter, MysqlJdbc.class);
    }

    @Override
    public long count(Filter filter) {
        return Long.parseLong(mysqlDatabase.mysqlJdbc.query(
                new SqlBuilder(tableName, MysqlJdbc.class).parseCountSql(filter)
        ).getFirst().get("C").toString());
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return mysqlDatabase.mysqlJdbc.query(
                new SqlBuilder(tableName, MysqlJdbc.class).parseQueryDataSql(query),
                ArrayUtil.asList("R")
        );
    }

    @Override
    public List<Column> getColumns() {
        return oracleTable.getColumns();
    }

    @Override
    public String getRemark() {
        return oracleTable.getRemark();
    }

    @Override
    public void rename(String newTableName) {
        oracleTable.rename(newTableName);
    }

}