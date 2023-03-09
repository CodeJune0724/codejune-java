package com.codejune.jdbc.mysql;

import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.oracle.OracleDatabase;
import com.codejune.jdbc.oracle.OracleTable;
import java.util.ArrayList;
import java.util.List;

/**
 * MysqlDatabase
 *
 * @author ZJ
 * */
public final class MysqlDatabase implements SqlDatabase {

    final MysqlJdbc mysqlJdbc;

    private final String databaseName;

    final OracleDatabase oracleDatabase;

    MysqlDatabase(MysqlJdbc mysqlJdbc, String databaseName) {
        this.mysqlJdbc = mysqlJdbc;
        this.databaseName = databaseName;
        this.oracleDatabase = mysqlJdbc.oracleJdbc.getDatabase(databaseName);
    }

    @Override
    public String getName() {
        return databaseName;
    }

    @Override
    public MysqlTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new InfoException("tableName is null");
        }
        return new MysqlTable(this, tableName);
    }

    @Override
    public List<MysqlTable> getTables() {
        List<MysqlTable> result = new ArrayList<>();
        for (OracleTable item : oracleDatabase.getTables()) {
            result.add(getTable(item.getName()));
        }
        return result;
    }

    @Override
    public void deleteTable(String tableName) {
        oracleDatabase.deleteTable(tableName);
    }

    @Override
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
            throw new InfoException("建表参数缺失");
        }
        String sql = "CREATE TABLE " + tableName + "(\n";
        sql = StringUtil.append(sql, ArrayUtil.toString(columnList, column -> {
            String result = "\t" + column.getName() + " ";
            result = switch (column.getDataType()) {
                case INT -> result + "INT";
                case STRING -> result + "VARCHAR(" + column.getLength() + ")";
                case DATE -> result + "DATETIME";
                case DOUBLE -> result + "DOUBLE";
                default -> throw new ErrorException("column.getDataType()未配置");
            };
            if (!column.isNullable()) {
                result = result + " NOT NULL";
            }
            if (column.isPrimaryKey()) {
                result = result + " PRIMARY KEY";
            }
            if (column.isAutoincrement()) {
                result = result + " AUTO_INCREMENT";
            }
            if (!StringUtil.isEmpty(column.getRemark())) {
                result = result + " COMMENT '" + column.getRemark() + "'";
            }
            return result;
        }, ",\n"), "\n)");
        if (!StringUtil.isEmpty(tableRemark)) {
            sql = StringUtil.append(sql, " COMMENT='" + tableRemark + "'");
        }
        mysqlJdbc.execute(sql);
    }

}