package com.codejune.jdbc.access;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
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
public final class AccessDatabaseDatabase extends SqlDatabase {

    AccessDatabaseDatabase(AccessDatabaseJdbc accessDatabaseJdbc) {
        super(accessDatabaseJdbc, null);
    }

    @Override
    public AccessDatabaseJdbc getJdbc() {
        return (AccessDatabaseJdbc) super.getJdbc();
    }

    @Override
    public AccessDatabaseTable getTable(String tableName) {
        return new AccessDatabaseTable(this, tableName);
    }

    @Override
    public List<AccessDatabaseTable> getTable() {
        try {
            List<AccessDatabaseTable> result = new ArrayList<>();
            for (String tableName : this.getJdbc().database.getTableNames()) {
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
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        try {
            if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
                throw new BaseException("建表参数缺失");
            }
            com.healthmarketscience.jackcess.Table table = this.getJdbc().database.getTable(tableName);
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
            tableBuilder.toTable(this.getJdbc().database);
            this.getJdbc().reload(true);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}