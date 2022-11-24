package com.codejune.jdbc.oracle;

import com.codejune.common.DataType;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
import com.codejune.jdbc.util.JdbcUtil;
import com.codejune.jdbc.util.SqlBuilder;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * OracleTable
 *
 * @author ZJ
 * */

public final class OracleTable implements SqlTable {

    private final OracleJdbc oracleJdbc;

    private final String tableName;

    private List<Column> columnList = null;

    OracleTable(OracleJdbc oracleJdbc, String tableName) {
        this.oracleJdbc = oracleJdbc;
        this.tableName = tableName;
    }

    @Override
    public List<Column> getColumns() {
        if (columnList == null) {
            columnList = oracleJdbc.getColumns(this.tableName);
        }
        if (columnList == null) {
            return null;
        }
        return new ArrayList<>(columnList);
    }

    @Override
    public String getRemark() {
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = this.oracleJdbc.getConnection().getMetaData();
            String schema;
            String originTableName;
            if (this.tableName.contains(".")) {
                String[] split = this.tableName.split("\\.");
                schema = split[0];
                originTableName = split[1];
            } else {
                schema = null;
                originTableName = this.tableName;
            }
            resultSet = metaData.getTables(null, schema == null ? null : schema.toUpperCase(), originTableName, new String[]{"TABLE", "REMARKS"});
            while (resultSet.next()) {
                return resultSet.getString("REMARKS");
            }
            return null;
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            JdbcUtil.close(resultSet);
        }
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        if (data.size() == 0) {
            return 0;
        }

        List<Column> allColumn = getColumns();
        if (ObjectUtil.isEmpty(allColumn)) {
            return 0;
        }

        Connection connection = oracleJdbc.getConnection();

        String sql = "INSERT INTO " + tableName + " (";
        String value = " VALUES (";
        int index = 0;
        for (Column column : allColumn) {
            index++;
            String end;

            if (index == allColumn.size()) {
                end = ")";
            } else {
                end = ", ";
            }

            sql = StringUtil.append(sql, column.getName(), end);
            value = StringUtil.append(value, "?", end);
        }
        sql = StringUtil.append(sql, value);

        PreparedStatement preparedStatement = null;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);
            int dataSize = data.size();
            for (int i = 0; i < dataSize; i++) {
                Map<String, Object> map = data.get(i);
                index = 0;
                for (Column column : allColumn) {
                    index++;
                    Object filedData = map.get(column.getName());
                    if (column.getDataType() != DataType.DATE) {
                        filedData = ObjectUtil.subString(filedData, column.getLength());
                    }
                    if (filedData == null) {
                        preparedStatement.setNull(index, column.getSqlType());
                    } else if (column.getDataType() == DataType.DATE) {
                        preparedStatement.setTimestamp(index, new Timestamp(((Date) DataType.transform(filedData, column.getDataType())).getTime()));
                    } else if (column.getDataType() == DataType.OBJECT) {
                        preparedStatement.setObject(index, filedData);
                    } else {
                        preparedStatement.setObject(index, DataType.transform(filedData, column.getDataType()));
                    }
                }
                preparedStatement.addBatch();
                if (i != 0 && i % 50000 == 0) {
                    preparedStatement.executeBatch();
                    connection.commit();
                    preparedStatement.clearBatch();
                }
            }
            preparedStatement.executeBatch();
            connection.commit();
            return dataSize;
        } catch (SQLException e) {
            throw new InfoException(e.getMessage() + ": " + sql);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JdbcUtil.close(preparedStatement);
        }
    }

    @Override
    public long delete(Filter filter) {
        return oracleJdbc.execute(new SqlBuilder(tableName, OracleJdbc.class).parseDeleteSql(filter));
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        if (ObjectUtil.isEmpty(setData)) {
            return 0;
        }

        // 根据字段类型转换数据
        Set<String> keySet = setData.keySet();
        List<Column> columnList = getColumns();
        if (columnList == null) {
            columnList = new ArrayList<>();
        }
        for (String key : keySet) {
            Object value = setData.get(key);
            Column column = null;
            for (Column item : columnList) {
                if (item.getName().equals(key)) {
                    column = item;
                    break;
                }
            }
            DataType dataType = null;
            if (column != null) {
                dataType = column.getDataType();
            }
            value = DataType.transform(value, dataType);
            setData.put(key, value);
        }

        return oracleJdbc.execute(new SqlBuilder(tableName, OracleJdbc.class).parseUpdateSql(setData, filter));
    }

    @Override
    public long count(Filter filter) {
        return Long.parseLong(oracleJdbc.queryBySql(
                new SqlBuilder(tableName, OracleJdbc.class).parseCountSql(filter)
        ).get(0).get("C").toString());
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return oracleJdbc.queryBySql(
                new SqlBuilder(tableName, OracleJdbc.class).parseQueryDataSql(query),
                ArrayUtil.parse("R")
        );
    }

}