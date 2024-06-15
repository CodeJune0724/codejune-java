package com.codejune.jdbc.access;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.oracle.OracleDatabase;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.TableBuilder;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

/**
 * AccessDatabaseDatabase
 *
 * @author ZJ
 * */
public final class AccessDatabaseDatabase implements SqlDatabase {

    final AccessDatabaseJdbc accessDatabaseJdbc;

    final OracleDatabase oracleDatabase;

    public AccessDatabaseDatabase(AccessDatabaseJdbc accessDatabaseJdbc) {
        this.accessDatabaseJdbc = accessDatabaseJdbc;
        this.oracleDatabase = accessDatabaseJdbc.oracleJdbc.getDatabase("");
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public AccessDatabaseTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new BaseException("tableName is null");
        }
        return new AccessDatabaseTable(this, tableName);
    }

    @Override
    public List<AccessDatabaseTable> getTables() {
        try {
            List<AccessDatabaseTable> result = new ArrayList<>();
            for (String tableName : this.accessDatabaseJdbc.database.getTableNames()) {
                if (tableName.startsWith("~")) {
                    continue;
                }
                result.add(getTable(tableName));
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public void deleteTable(String tableName) {
        oracleDatabase.deleteTable(tableName);
        accessDatabaseJdbc.reload(true);
    }

    @Override
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        try {
            if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
                throw new BaseException("建表参数缺失");
            }
            com.healthmarketscience.jackcess.Table table = accessDatabaseJdbc.database.getTable(tableName);
            if (table != null) {
                throw new BaseException(tableName + "表已存在");
            }
            TableBuilder tableBuilder = new TableBuilder(tableName);
            List<ColumnBuilder> columnBuilderList = new ArrayList<>();
            for (Column column : columnList) {
                ColumnBuilder columnBuilder = new ColumnBuilder(column.getName());
                columnBuilder.setSQLType(column.getType().getVendorTypeNumber());
                if (column.isPrimaryKey()) {
                    tableBuilder.setPrimaryKey(column.getName());
                }
                if (column.isAutoincrement()) {
                    columnBuilder.setAutoNumber(true);
                    columnBuilder.setSQLType(JDBCType.BIGINT.getVendorTypeNumber());
                }
                if (column.getLength() > 0) {
                    columnBuilder.setLength(column.getLength());
                }
                columnBuilderList.add(columnBuilder);
            }
            for (ColumnBuilder columnBuilder : columnBuilderList) {
                tableBuilder.addColumn(columnBuilder);
            }
            tableBuilder.toTable(accessDatabaseJdbc.database);
            accessDatabaseJdbc.reload(true);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}