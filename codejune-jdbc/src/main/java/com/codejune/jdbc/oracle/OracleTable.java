package com.codejune.jdbc.oracle;

import com.codejune.Jdbc;
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

    private final OracleDatabase oracleDatabase;

    private final String tableName;

    OracleTable(OracleDatabase oracleDatabase, String tableName) {
        this.oracleDatabase = oracleDatabase;
        this.tableName = tableName;
    }

    @Override
    public List<Column> getColumns() {
        List<Column> result = new ArrayList<>();
        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = oracleDatabase.oracleJdbc.getConnection().getMetaData();
        } catch (Exception e) {
            throw new InfoException(e);
        }
        ResultSet primaryKeyResultSet = null;
        List<String> primaryKeyList = new ArrayList<>();
        try {
            primaryKeyResultSet = databaseMetaData.getPrimaryKeys(oracleDatabase.getName(), oracleDatabase.getName(), tableName);
            while (primaryKeyResultSet.next()) {
                primaryKeyList.add(primaryKeyResultSet.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            JdbcUtil.close(primaryKeyResultSet);
        }
        ResultSet columnResultSet = null;
        try {
            columnResultSet = databaseMetaData.getColumns(oracleDatabase.getName(), oracleDatabase.getName(), tableName, null);
            ResultSetMetaData resultSetMetaData = columnResultSet.getMetaData();
            List<String> columnResultSetColumnList = new ArrayList<>();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                columnResultSetColumnList.add(resultSetMetaData.getColumnName(i));
            }
            while (columnResultSet.next()) {
                String name = columnResultSet.getString("COLUMN_NAME");
                Column column = new Column(name, columnResultSet.getInt("DATA_TYPE"));
                column.setRemark(columnResultSet.getString("REMARKS"));
                column.setLength(columnResultSet.getInt("COLUMN_SIZE"));
                column.setPrimaryKey(primaryKeyList.contains(name));
                column.setNullable("YES".equals(columnResultSet.getString("IS_NULLABLE")));
                if (columnResultSetColumnList.contains("IS_AUTOINCREMENT")) {
                    column.setAutoincrement("YES".equals(columnResultSet.getString("IS_AUTOINCREMENT")));
                }
                result.add(column);
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            JdbcUtil.close(columnResultSet);
        }
    }

    @Override
    public String getRemark() {
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = oracleDatabase.oracleJdbc.getConnection().getMetaData();
            resultSet = metaData.getTables(oracleDatabase.getName(), oracleDatabase.getName(), tableName, new String[]{"TABLE", "REMARKS"});
            if (resultSet.next()) {
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
    public void rename(String newTableName) {
        if (StringUtil.isEmpty(newTableName)) {
            return;
        }
        oracleDatabase.oracleJdbc.execute("ALTER TABLE " + tableName + " RENAME TO " + newTableName);
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
        Connection connection = oracleDatabase.oracleJdbc.getConnection();
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
        return oracleDatabase.oracleJdbc.execute(new SqlBuilder(tableName, OracleJdbc.class).parseDeleteSql(filter));
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return update(setData, filter, OracleJdbc.class);
    }

    @Override
    public long count(Filter filter) {
        return Long.parseLong(oracleDatabase.oracleJdbc.query(
                new SqlBuilder(tableName, OracleJdbc.class).parseCountSql(filter)
        ).get(0).get("C").toString());
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return oracleDatabase.oracleJdbc.query(
                new SqlBuilder(tableName, OracleJdbc.class).parseQueryDataSql(query),
                ArrayUtil.parse("R")
        );
    }

    public long update(Map<String, Object> setData, Filter filter, Class<? extends Jdbc> jdbcType) {
        if (ObjectUtil.isEmpty(setData)) {
            return 0;
        }
        List<Column> allColumn = getColumns();
        if (ObjectUtil.isEmpty(allColumn)) {
            return 0;
        }
        String sql = "UPDATE " + tableName + " SET " + ArrayUtil.toString(setData.keySet(), key -> key + " = ?", ", ") + " " + new SqlBuilder(tableName, jdbcType).parseWhere(filter);
        Connection connection = oracleDatabase.oracleJdbc.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int index = 0;
            for (String key : setData.keySet()) {
                index++;
                Object data = setData.get(key);
                Column column = null;
                for (Column item : allColumn) {
                    if (item.getName().equals(key)) {
                        column = item;
                        break;
                    }
                }
                if (column == null) {
                    throw new InfoException(key + "字段不存在");
                }
                if (data == null) {
                    preparedStatement.setNull(index, column.getSqlType());
                } else if (column.getDataType() == DataType.DATE) {
                    preparedStatement.setTimestamp(index, new Timestamp(((Date) DataType.transform(data, column.getDataType())).getTime()));
                } else if (column.getDataType() == DataType.OBJECT) {
                    preparedStatement.setObject(index, data);
                } else {
                    preparedStatement.setObject(index, DataType.transform(data, column.getDataType()));
                }
            }
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

}