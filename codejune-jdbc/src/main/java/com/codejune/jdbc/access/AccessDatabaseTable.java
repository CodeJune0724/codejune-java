package com.codejune.jdbc.access;

import com.codejune.common.DataType;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Filter;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.jdbc.oracle.OracleJdbc;
import com.codejune.jdbc.oracle.OracleTable;
import com.codejune.jdbc.table.SqlTable;
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.TableBuilder;
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
        try {
            if (ObjectUtil.isEmpty(columnList)) {
                throw new InfoException("字段不能为空");
            }

            com.healthmarketscience.jackcess.Table table = accessDatabaseJdbc.database.getTable(tableName);

            // 所有字段
            List<ColumnBuilder> columnBuilderList = new ArrayList<>();
            int p = 0;
            int index = 0;
            for (Column column : columnList) {
                ColumnBuilder columnBuilder = new ColumnBuilder(column.getName());
                DataType dataType = column.getDataType();
                if (column.isPrimaryKey()) {
                    columnBuilder.setAutoNumber(true).setSQLType(Types.BIGINT);
                    p = index;
                } else if (dataType == DataType.INT) {
                    columnBuilder.setSQLType(Types.INTEGER);
                } else if (dataType == DataType.STRING) {
                    columnBuilder.setSQLType(Types.VARCHAR);
                } else if (dataType == DataType.LONG_STRING) {
                    columnBuilder.setSQLType(Types.LONGVARCHAR);
                } else if (dataType == DataType.DATE) {
                    columnBuilder.setSQLType(Types.DATE);
                } else if (dataType == DataType.BOOLEAN) {
                    columnBuilder.setSQLType(Types.BOOLEAN);
                } else {
                    throw new ErrorException(dataType + "未配置");
                }
                columnBuilderList.add(columnBuilder);
                index++;
            }
            ArrayUtil.move(columnBuilderList, p, 0);

            // 获取表数据
            List<Map<String, Object>> tableData;
            if (table == null) {
                tableData = new ArrayList<>();
            } else {
                tableData = query().getData();
                accessDatabaseJdbc.execute("DROP TABLE " + tableName);
                accessDatabaseJdbc.reload(true);
            }

            // 建表
            TableBuilder tableBuilder = new TableBuilder(tableName);
            for (ColumnBuilder columnBuilder : columnBuilderList) {
                tableBuilder.addColumn(columnBuilder);
            }
            tableBuilder.toTable(accessDatabaseJdbc.database);
            accessDatabaseJdbc.reload(true);

            // 保存数据
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
     * @param isCase 时候区分大小写
     *
     * @return QueryResult
     * */
    public QueryResult<Map<String, Object>> query(Query query, boolean isCase) {
        OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
        return table.query(query, isCase ? AccessDatabaseJdbc.class : null);
    }

    @Override
    public List<Column> getColumns() {
        List<Column> result = new ArrayList<>();
        List<? extends com.healthmarketscience.jackcess.Column> columns;
        try {
            columns = this.accessDatabaseJdbc.database.getTable(tableName).getColumns();
        } catch (Exception e) {
            throw new InfoException(e);
        }
        for (com.healthmarketscience.jackcess.Column column : columns) {
            String name = column.getName();
            int sqlType;
            int length = column.getLength();
            boolean isPrimaryKey = column.isAutoNumber();
            try {
                sqlType = column.getSQLType();
            } catch (Exception e) {
                throw new InfoException(e);
            }

            result.add(new Column(name, null, sqlType, length, isPrimaryKey));
        }
        return result;
    }

    @Override
    public String getRemark() {
        return new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName).getRemark();
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
        if (table == null) {
            return 0;
        }
        synchronized (OBJECT) {
            return table.insert(data);
        }
    }

    @Override
    public long delete(Filter filter) {
        OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
        if (table == null) {
            return 0;
        }
        return table.delete(filter);
    }

    @Override
    public long update(Filter filter, Map<String, Object> setData) {
        OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
        if (table == null) {
            return 0;
        }
        return table.update(filter, setData);
    }

    @Override
    public QueryResult<Map<String, Object>> query(Query query) {
        return query(query, true);
    }

    @Override
    public QueryResult<Map<String, Object>> query() {
        return query(null);
    }

}