package com.codejune.jdbc.table;

import com.codejune.Jdbc;
import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.SqlJdbc;
import com.codejune.jdbc.Table;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.util.SqlBuilder;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SqlTable implements Table {

    private final SqlDatabase sqlDatabase;

    private final String name;

    public SqlTable(SqlDatabase sqlDatabase, String name) {
        this.sqlDatabase = sqlDatabase;
        this.name = name;
    }

    @Override
    public SqlDatabase getDatabase() {
        return this.sqlDatabase;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        if (ObjectUtil.isEmpty(data)) {
            return 0;
        }
        List<Column> allColumn = this.getColumn();
        if (ObjectUtil.isEmpty(allColumn)) {
            return 0;
        }
        String sql = "INSERT INTO " + this.getName() + " (" + ArrayUtil.toString(allColumn, Column::getName, ", ") + ") VALUES (" + ArrayUtil.toString(ArrayUtil.createSequence(allColumn.size()), integer -> "?", ", ") + ")";
        Connection connection = this.getDatabase().getJdbc().getConnection();
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
                        preparedStatement.setTimestamp(index, new Timestamp(ObjectUtil.parse(filedData, Date.class).getTime()));
                    } else if (column.getType() == JDBCType.DATE) {
                        preparedStatement.setDate(index, new java.sql.Date(ObjectUtil.parse(filedData, Date.class).getTime()));
                    } else if (column.getType() == JDBCType.TIME) {
                        preparedStatement.setTime(index, new Time(ObjectUtil.parse(filedData, Date.class).getTime()));
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
        return this.getDatabase().getJdbc().execute(new SqlBuilder(this.name, SqlJdbc.class).parseDeleteSql(filter));
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        return this.update(setData, filter, SqlJdbc.class);
    }

    @Override
    public long count(Filter filter) {
        return ObjectUtil.parse(this.getDatabase().getJdbc().query(new SqlBuilder(this.name, SqlJdbc.class).parseCountSql(filter)).getFirst().get("C"), Long.class);
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        return this.getDatabase().getJdbc().query(new SqlBuilder(this.name, SqlJdbc.class).parseQueryDataSql(query), ArrayUtil.asList("R"));
    }

    /**
     * 获取所有字段
     *
     * @return 所有字段
     * */
    public List<Column> getColumn() {
        List<Column> result = new ArrayList<>();
        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = this.getDatabase().getJdbc().getConnection().getMetaData();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        List<String> primaryKeyList = new ArrayList<>();
        try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(this.getDatabase().getName(), this.getDatabase().getName(), this.getName())) {
            while (resultSet.next()) {
                primaryKeyList.add(resultSet.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try (ResultSet resultSet = databaseMetaData.getColumns(this.getDatabase().getName(), this.getDatabase().getName(), this.getName(), null)) {
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

    /**
     * 获取字段
     *
     * @param columnName 字段名
     *
     * @return 类型
     * */
    public final Column getColumn(String columnName) {
        List<Column> columns = this.getColumn();
        if (columns == null || StringUtil.isEmpty(columnName)) {
            return null;
        }
        for (Column column : columns) {
            if (columnName.equals(column.getName())) {
                return column;
            }
        }
        return null;
    }

    /**
     * 获取表备注
     *
     * @return 表备注
     * */
    public String getRemark() {
        DatabaseMetaData metaData;
        try {
            metaData = this.getDatabase().getJdbc().getConnection().getMetaData();
        } catch (Exception e) {
            throw new BaseException(e);
        }
        try (ResultSet resultSet = metaData.getTables(this.getDatabase().getName(), this.getDatabase().getName(), this.getName(), new String[]{"TABLE", "REMARKS"})) {
            if (resultSet.next()) {
                return resultSet.getString("REMARKS");
            }
            return null;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 修改表名
     *
     * @param newTableName 新表名
     * */
    public void rename(String newTableName) {
        if (StringUtil.isEmpty(newTableName)) {
            return;
        }
        this.getDatabase().getJdbc().execute("ALTER TABLE " + this.name + " RENAME TO " + newTableName);
    }

    protected final long update(Map<String, Object> setData, Filter filter, Class<? extends Jdbc> jdbcType) {
        if (ObjectUtil.isEmpty(setData)) {
            return 0;
        }
        List<Column> allColumn = this.getColumn();
        if (ObjectUtil.isEmpty(allColumn)) {
            return 0;
        }
        String sql = "UPDATE " + this.getName() + " SET " + ArrayUtil.toString(setData.keySet(), key -> key + " = ?", ", ") + " " + new SqlBuilder(this.getName(), jdbcType).parseWhere(filter);
        Connection connection = this.getDatabase().getJdbc().getConnection();
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
                    preparedStatement.setTimestamp(index, new Timestamp(ObjectUtil.parse(data, Date.class).getTime()));
                } else if (column.getType() == JDBCType.DATE) {
                    preparedStatement.setDate(index, new java.sql.Date(ObjectUtil.parse(data, Date.class).getTime()));
                } else if (column.getType() == JDBCType.TIME) {
                    preparedStatement.setTime(index, new Time(ObjectUtil.parse(data, Date.class).getTime()));
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