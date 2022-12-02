package com.codejune.jdbc.access;

import com.codejune.common.DataType;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.oracle.OracleDatabase;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.TableBuilder;
import java.sql.Types;
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
            throw new InfoException("tableName is null");
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
            throw new InfoException(e.getMessage());
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
                throw new InfoException("建表参数缺失");
            }
            com.healthmarketscience.jackcess.Table table = accessDatabaseJdbc.database.getTable(tableName);
            if (table != null) {
                throw new InfoException(tableName + "表已存在");
            }
            List<ColumnBuilder> columnBuilderList = new ArrayList<>();
            for (Column column : columnList) {
                ColumnBuilder columnBuilder = new ColumnBuilder(column.getName());
                DataType dataType = column.getDataType();
                if (column.isAutoincrement()) {
                    columnBuilder.setAutoNumber(true);
                    dataType = DataType.LONG;
                }
                if (dataType == com.codejune.common.DataType.INT) {
                    columnBuilder.setSQLType(Types.INTEGER);
                } else if (dataType == DataType.LONG) {
                    columnBuilder.setSQLType(Types.BIGINT);
                }else if (dataType == com.codejune.common.DataType.STRING) {
                    columnBuilder.setSQLType(Types.VARCHAR);
                } else if (dataType == com.codejune.common.DataType.LONG_STRING) {
                    columnBuilder.setSQLType(Types.LONGVARCHAR);
                } else if (dataType == com.codejune.common.DataType.DATE) {
                    columnBuilder.setSQLType(Types.DATE);
                } else if (dataType == DataType.BOOLEAN) {
                    columnBuilder.setSQLType(Types.BOOLEAN);
                } else {
                    throw new ErrorException(dataType + "未配置");
                }
                if (column.getLength() > 0) {
                    columnBuilder.setLength(column.getLength());
                }
                columnBuilderList.add(columnBuilder);
            }
            TableBuilder tableBuilder = new TableBuilder(tableName);
            for (ColumnBuilder columnBuilder : columnBuilderList) {
                tableBuilder.addColumn(columnBuilder);
            }
            tableBuilder.toTable(accessDatabaseJdbc.database);
            accessDatabaseJdbc.reload(true);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}