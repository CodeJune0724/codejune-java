package com.codejune.jdbc.database;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.*;
import com.codejune.jdbc.table.SqlTable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlDatabase
 *
 * @author ZJ
 * */
public class SqlDatabase implements Database {

    private final SqlJdbc sqlJdbc;

    private final String name;

    protected SqlDatabase(SqlJdbc sqlJdbc, String name) {
        this.sqlJdbc = sqlJdbc;
        this.name = name;
    }

    @Override
    public SqlJdbc getJdbc() {
        return this.sqlJdbc;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public SqlTable getTable(String tableName) {
        return new SqlTable(this, tableName);
    }

    @Override
    public List<? extends SqlTable> getTable() {
        List<SqlTable> result = new ArrayList<>();
        DatabaseMetaData metaData;
        try {
            metaData = this.getJdbc().getConnection().getMetaData();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try (ResultSet resultSet = metaData.getTables(this.getName(), this.getName() == null ? null : this.getName().toUpperCase(), null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                result.add(getTable(resultSet.getString("TABLE_NAME")));
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public final void deleteTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return;
        }
        this.getJdbc().execute("DROP TABLE " + tableName);
    }

    /**
     * 新建表
     *
     * @param tableName 表名
     * @param tableRemark 表备注
     * @param columnList columnList
     * */
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
            throw new BaseException("建表参数缺失");
        }
        String sql = "CREATE TABLE " + tableName + "(\n";
        sql = StringUtil.append(sql, ArrayUtil.toString(columnList, column -> {
            String result = "\t" + column.getName() + " ";
            result = switch (column.getType()) {
                case INTEGER -> result + "NUMBER(" + column.getLength() + ")";
                case VARCHAR -> result + "VARCHAR2(" + column.getLength() + ")";
                case DATE -> result + "DATETIME";
                default -> "\t" + column.getName() + " ";
            };
            if (!column.isNullable()) {
                result = result + " NOT NULL";
            }
            if (column.isPrimaryKey()) {
                result = result + " PRIMARY KEY";
            }
            return result;
        }, ",\n"), "\n)");
        this.getJdbc().execute(sql);
        if (!StringUtil.isEmpty(tableRemark)) {
            this.getJdbc().execute("COMMENT ON TABLE " + tableName + " IS '" + tableRemark + "'");
        }
        for (Column column : columnList) {
            if (!StringUtil.isEmpty(column.getRemark())) {
                this.getJdbc().execute("COMMENT ON COLUMN " + tableName + "." + column.getName() + " IS '" + column.getRemark() + "'");
            }
        }
    }

}