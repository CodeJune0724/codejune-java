package com.codejune.jdbc.access;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.oracle.OracleTable;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AccessDatabaseTable
 *
 * @author ZJ
 * */
public final class AccessDatabaseTable implements SqlTable {

    private final AccessDatabaseDatabase accessDatabaseDatabase;

    private final String tableName;

    private static final Object LOCK = new Object();

    AccessDatabaseTable(AccessDatabaseDatabase accessDatabaseDatabase, String tableName) {
        this.accessDatabaseDatabase = accessDatabaseDatabase;
        this.tableName = tableName;
    }

    /**
     * 重新加载表信息
     *
     * @param columnList 字段
     * */
    public void reloadTable(List<Column> columnList) {
        if (ObjectUtil.isEmpty(columnList)) {
            throw new BaseException("字段不能为空");
        }
        try {
            com.healthmarketscience.jackcess.Table table = accessDatabaseDatabase.accessDatabaseJdbc.database.getTable(tableName);
            if (table != null) {
                List<Column> columns = getColumns();
                boolean exist = true;
                for (Column column : columnList) {
                    boolean columnExist = false;
                    for (Column originColumn : columns) {
                        JDBCType originType = originColumn.getType();
                        if (originType == JDBCType.TIMESTAMP) {
                            originType = JDBCType.DATE;
                        }
                        if (originColumn.getName().equals(column.getName()) && originType == column.getType()) {
                            columnExist = true;
                            break;
                        }
                    }
                    if (!columnExist) {
                        exist = false;
                        break;
                    }
                }
                if (exist) {
                    return;
                }
            }
            List<Map<String, Object>> tableData;
            if (table == null) {
                tableData = new ArrayList<>();
            } else {
                tableData = query().getData();
                accessDatabaseDatabase.deleteTable(tableName);
            }
            accessDatabaseDatabase.createTable(tableName, null, columnList);
            this.insert(tableData);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public List<Column> getColumns() {
        List<Column> result = new ArrayList<>();
        List<? extends com.healthmarketscience.jackcess.Column> columns;
        try {
            columns = accessDatabaseDatabase.accessDatabaseJdbc.database.getTable(tableName).getColumns();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        for (com.healthmarketscience.jackcess.Column jackcessColumn : columns) {
            String name = jackcessColumn.getName();
            JDBCType jdbcType;
            int length = jackcessColumn.getLength();
            boolean isPrimaryKey = jackcessColumn.isAutoNumber();
            try {
                jdbcType = JDBCType.valueOf(jackcessColumn.getSQLType());
                if (jackcessColumn.getType() == com.healthmarketscience.jackcess.DataType.BOOLEAN) {
                    jdbcType = JDBCType.BOOLEAN;
                }
            } catch (Exception e) {
                throw new BaseException(e);
            }
            Column column = new Column(name, jdbcType);
            column.setLength(length);
            column.setPrimaryKey(isPrimaryKey);
            result.add(column);
        }
        return result;
    }

    @Override
    public String getRemark() {
        return getOracleTable().getRemark();
    }

    @Override
    public void rename(String newTableName) {
        getOracleTable().rename(newTableName);
        accessDatabaseDatabase.accessDatabaseJdbc.reload(true);
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        synchronized (LOCK) {
            return getOracleTable().insert(data);
        }
    }

    @Override
    public long delete(Filter filter) {
        return getOracleTable().delete(filter);
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return getOracleTable().update(setData, filter);
    }

    @Override
    public long count(Filter filter) {
        return getOracleTable().count(filter);
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return getOracleTable().queryData(query);
    }

    private OracleTable getOracleTable() {
        return accessDatabaseDatabase.accessDatabaseJdbc.oracleJdbc.getDefaultDatabase().getTable(tableName);
    }

}