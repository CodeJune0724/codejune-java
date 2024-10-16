package com.codejune.jdbc.access;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.Column;
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
public final class AccessDatabaseTable extends SqlTable {

    private static final Object LOCK = new Object();

    AccessDatabaseTable(AccessDatabaseDatabase accessDatabaseDatabase, String name) {
        super(accessDatabaseDatabase, name);
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
            com.healthmarketscience.jackcess.Table table = this.getDatabase().getJdbc().database.getTable(this.getName());
            if (table != null) {
                List<Column> columns = this.getColumn();
                boolean exist = true;
                for (Column column : columnList) {
                    boolean columnExist = false;
                    for (Column originColumn : columns) {
                        JDBCType originType = originColumn.getType();
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
                this.getDatabase().deleteTable(this.getName());
            }
            this.getDatabase().createTable(this.getName(), null, columnList);
            this.insert(tableData);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public AccessDatabaseDatabase getDatabase() {
        return (AccessDatabaseDatabase) super.getDatabase();
    }

    @Override
    public List<Column> getColumn() {
        List<Column> result = new ArrayList<>();
        List<? extends com.healthmarketscience.jackcess.Column> columns;
        try {
            columns = this.getDatabase().getJdbc().database.getTable(this.getName()).getColumns();
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
    public void rename(String newTableName) {
        super.rename(newTableName);
        this.getDatabase().getJdbc().reload(true);
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        synchronized (LOCK) {
            return super.insert(data);
        }
    }

    @Override
    public long delete(Filter filter) {
        synchronized (LOCK) {
            return super.delete(filter);
        }
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        synchronized (LOCK) {
            return super.update(setData, filter);
        }
    }

}