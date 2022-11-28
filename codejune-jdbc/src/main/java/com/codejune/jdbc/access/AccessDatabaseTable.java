package com.codejune.jdbc.access;

import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.jdbc.oracle.OracleJdbc;
import com.codejune.jdbc.oracle.OracleTable;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import com.codejune.jdbc.util.SqlBuilder;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AccessDatabaseTable
 *
 * @author ZJ
 * */
public final class AccessDatabaseTable implements SqlTable {

    private final AccessDatabaseJdbc accessDatabaseJdbc;

    private final String tableName;

    private static final Object OBJECT = new Object();

    AccessDatabaseTable(AccessDatabaseJdbc accessDatabaseJdbc, String tableName) {
        this.accessDatabaseJdbc = accessDatabaseJdbc;
        this.tableName = tableName;
    }

    /**
     * 重新加载表信息
     *
     * @param columnList 字段
     * */
    public void reloadTable(List<Column> columnList) {
        if (ObjectUtil.isEmpty(columnList)) {
            throw new InfoException("字段不能为空");
        }
        try {
            com.healthmarketscience.jackcess.Table table = accessDatabaseJdbc.database.getTable(tableName);
            if (table != null) {
                List<Column> columns = getColumns();
                boolean exist = true;
                for (Column column : columnList) {
                    boolean columnExist = false;
                    for (Column originColumn : columns) {
                        if (originColumn.getName().equals(column.getName()) && originColumn.getDataType() == column.getDataType()) {
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
                accessDatabaseJdbc.deleteTable(tableName);
            }
            accessDatabaseJdbc.createTable(tableName, null, columnList);
            this.insert(tableData);
        } catch (IOException e) {
            throw new ErrorException(e.getMessage());
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * 查询
     *
     * @param query query
     * @param isCase 是否区分大小写
     *
     * @return QueryResult
     * */
    public QueryResult<Map<String, Object>> query(Query query, boolean isCase) {
        if (query == null) {
            query = new Query();
        }
        QueryResult<Map<String, Object>> result = new QueryResult<>();
        result.setCount(count(query.getFilter(), isCase));
        result.setData(queryData(query, isCase));
        return result;
    }

    /**
     * 统计
     *
     * @param filter filter
     * @param isCase 是否区分大小写
     *
     * @return 数量
     * */
    public long count(Filter filter, boolean isCase) {
        return Long.parseLong(accessDatabaseJdbc.queryBySql(
                new SqlBuilder(tableName, isCase ? AccessDatabaseJdbc.class : OracleJdbc.class).parseCountSql(filter)
        ).get(0).get("C").toString());
    }

    /**
     * 获取数据
     *
     * @param query query
     * @param isCase 是否区分大小写
     *
     * @return 数量
     * */
    public List<Map<String, Object>> queryData(Query query, boolean isCase) {
        return accessDatabaseJdbc.queryBySql(
                new SqlBuilder(tableName, isCase ? AccessDatabaseJdbc.class : OracleJdbc.class).parseQueryDataSql(query),
                ArrayUtil.parse("R")
        );
    }

    @Override
    public List<Column> getColumns() {
        List<Column> result = accessDatabaseJdbc.oracleJdbc.getColumnCache(tableName);
        if (result == null) {
            result = new ArrayList<>();
            List<? extends com.healthmarketscience.jackcess.Column> columns;
            try {
                columns = this.accessDatabaseJdbc.database.getTable(tableName).getColumns();
            } catch (Exception e) {
                throw new InfoException(e);
            }
            for (com.healthmarketscience.jackcess.Column jackcessColumn : columns) {
                String name = jackcessColumn.getName();
                int sqlType;
                int length = jackcessColumn.getLength();
                boolean isPrimaryKey = jackcessColumn.isAutoNumber();
                try {
                    sqlType = jackcessColumn.getSQLType();
                    if (jackcessColumn.getType() == com.healthmarketscience.jackcess.DataType.BOOLEAN) {
                        sqlType = Types.BOOLEAN;
                    }
                } catch (Exception e) {
                    throw new InfoException(e);
                }
                Column column = new Column(name, sqlType);
                column.setLength(length);
                column.setPrimaryKey(isPrimaryKey);
                result.add(column);
            }
            accessDatabaseJdbc.oracleJdbc.setColumnCache(tableName, result);
        }
        return ObjectUtil.clone(result);
    }

    @Override
    public String getRemark() {
        return accessDatabaseJdbc.oracleJdbc.getTable(tableName).getRemark();
    }

    @Override
    public void rename(String newTableName) {
        accessDatabaseJdbc.oracleJdbc.getTable(tableName).rename(newTableName);
        accessDatabaseJdbc.reload(true);
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        OracleTable table = accessDatabaseJdbc.oracleJdbc.getTable(tableName);
        synchronized (OBJECT) {
            return table.insert(data);
        }
    }

    @Override
    public long delete(Filter filter) {
        OracleTable table = accessDatabaseJdbc.oracleJdbc.getTable(tableName);
        return table.delete(filter);
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        OracleTable table = accessDatabaseJdbc.oracleJdbc.getTable(tableName);
        return table.update(setData, filter);
    }

    @Override
    public long count(Filter filter) {
        return count(filter, true);
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return queryData(query, true);
    }

}