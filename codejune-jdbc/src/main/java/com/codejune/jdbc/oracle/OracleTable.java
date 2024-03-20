package com.codejune.jdbc.oracle;

import com.codejune.Jdbc;
import com.codejune.common.Data;
import com.codejune.common.BaseException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.table.SqlTable;
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
            throw new BaseException(e);
        }
        List<String> primaryKeyList = new ArrayList<>();
        try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(oracleDatabase.getName(), oracleDatabase.getName(), tableName)) {
            while (resultSet.next()) {
                primaryKeyList.add(resultSet.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try (ResultSet resultSet = databaseMetaData.getColumns(oracleDatabase.getName(), oracleDatabase.getName(), tableName, null)) {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            List<String> columnResultSetColumnList = new ArrayList<>();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                columnResultSetColumnList.add(resultSetMetaData.getColumnName(i));
            }
            while (resultSet.next()) {
                String name = resultSet.getString("COLUMN_NAME");
                Column column = new Column(name, JDBCType.valueOf(resultSet.getInt("DATA_TYPE")));
                column.setRemark(resultSet.getString("REMARKS"));
                column.setLength(resultSet.getInt("COLUMN_SIZE"));
                column.setPrimaryKey(primaryKeyList.contains(name));
                column.setNullable("YES".equals(resultSet.getString("IS_NULLABLE")));
                if (columnResultSetColumnList.contains("IS_AUTOINCREMENT")) {
                    column.setAutoincrement("YES".equals(resultSet.getString("IS_AUTOINCREMENT")));
                }
                result.add(column);
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public String getRemark() {
        DatabaseMetaData metaData;
        try {
            metaData = oracleDatabase.oracleJdbc.getConnection().getMetaData();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try (ResultSet resultSet = metaData.getTables(oracleDatabase.getName(), oracleDatabase.getName(), tableName, new String[]{"TABLE", "REMARKS"})) {
            if (resultSet.next()) {
                return resultSet.getString("REMARKS");
            }
            return null;
        } catch (Exception e) {
            throw new BaseException(e);
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
        String sql = "INSERT INTO " + tableName + " (" + ArrayUtil.toString(allColumn, Column::getName, ", ") + ") VALUES (" + ArrayUtil.toString(ArrayUtil.createSequence(allColumn.size()), integer -> "?", ", ") + ")";
        Connection connection = oracleDatabase.oracleJdbc.getConnection();
        boolean AutoCommit = true;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            AutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            int dataSize = data.size();
            for (int i = 0; i < dataSize; i++) {
                Map<String, Object> map = data.get(i);
                int index = 0;
                for (Column column : allColumn) {
                    index++;
                    Object filedData = map.get(column.getName());
                    if (filedData == null) {
                        preparedStatement.setNull(index, column.getType().getVendorTypeNumber());
                    } else if (column.getType() == JDBCType.TIMESTAMP) {
                        preparedStatement.setTimestamp(index, new Timestamp(((Date) Data.transform(filedData, Date.class)).getTime()));
                    } else {
                        preparedStatement.setObject(index, filedData);
                    }
                }
                preparedStatement.addBatch();
                if ((i + 1) % 50000 == 0) {
                    preparedStatement.executeBatch();
                    connection.commit();
                    preparedStatement.clearBatch();
                }
            }
            preparedStatement.executeBatch();
            connection.commit();
            return dataSize;
        } catch (SQLException e) {
            throw new BaseException(e.getMessage() + ": " + sql);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(AutoCommit);
            } catch (Exception ignored) {}
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
                ArrayUtil.asList("R")
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
                    throw new BaseException(key + "字段不存在");
                }
                if (data == null) {
                    preparedStatement.setNull(index, column.getType().getVendorTypeNumber());
                } else if (column.getType() == JDBCType.TIMESTAMP) {
                    preparedStatement.setTimestamp(index, new Timestamp(((Date) Data.transform(data, Date.class)).getTime()));
                } else {
                    preparedStatement.setObject(index, data);
                }
            }
            return preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}