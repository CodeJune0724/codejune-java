package com.codejune.jdbc.sqlite;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.table.SqlTable;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

/**
 * SqliteDatabase
 *
 * @author ZJ
 * */
public final class SqliteDatabase extends SqlDatabase {

    SqliteDatabase(SqliteJdbc sqliteJdbc) {
        super(sqliteJdbc, null);
    }

    @Override
    public SqliteJdbc getJdbc() {
        return (SqliteJdbc) super.getJdbc();
    }

    @Override
    public SqliteTable getTable(String tableName) {
        return new SqliteTable(this, tableName);
    }

    @Override
    public List<SqliteTable> getTable() {
        List<SqliteTable> result = new ArrayList<>();
        for (SqlTable sqlTable : super.getTable()) {
            result.add(this.getTable(sqlTable.getName()));
        }
        return result;
    }

    @Override
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
            throw new BaseException("建表参数缺失");
        }
        String sql = "CREATE TABLE " + tableName + " (\n";
        sql = StringUtil.append(sql, ArrayUtil.toString(columnList, column -> {
            String result = "\t" + column.getName() + " ";
            if (column.getType() == JDBCType.INTEGER) {
                result = result + "INTEGER";
            } else {
                result = result + "TEXT";
            }
            if (!column.isNullable()) {
                result = result + " NOT NULL";
            }
            if (column.isPrimaryKey()) {
                result = result + " PRIMARY KEY";
            }
            if (column.isAutoincrement()) {
                result = result + " AUTOINCREMENT";
            }
            return result;
        }, ",\n"), "\n)");
        this.getJdbc().execute(sql);
    }

}