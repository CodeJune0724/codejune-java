package com.codejune.jdbc;

import com.codejune.Jdbc;
import com.codejune.core.BaseException;
import com.codejune.core.util.StringUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 关系型数据库
 *
 * @author ZJ
 * */
public abstract class SqlJdbc implements Jdbc {

    private Connection connection;

    public SqlJdbc(Connection connection) {
        this.connection = connection;
    }

    /**
     * 获取Connection
     *
     * @return Connection
     * */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 设置Connection
     *
     * @param connection connection
     * */
    public void setConnection(Connection connection) {
        this.close();
        this.connection = connection;
    }

    /**
     * 执行sql
     *
     * @param sql sql
     *
     * @return 受影响的行数
     * */
    public final long execute(String sql) {
        if (StringUtil.isEmpty(sql)) {
            return 0;
        }
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 通过sql查询
     *
     * @param sql sql
     * @param filterFields 过滤的字段
     *
     * @return List
     * */
    public final List<Map<String, Object>> query(String sql, List<String> filterFields) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (
                PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(resultSetMetaData.getColumnLabel(i));
            }
            while (resultSet.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (String column : columns) {
                    if (filterFields != null && filterFields.contains(column)) {
                        continue;
                    }
                    map.put(column, resultSet.getObject(column));
                }
                result.add(map);
            }
        } catch (SQLException e) {
            throw new BaseException(e.getMessage() + "：" + sql);
        }
        return result;
    }

    /**
     * 通过sql查询
     *
     * @param sql sql
     *
     * @return List
     * */
    public final List<Map<String, Object>> query(String sql) {
        return query(sql, null);
    }

    /**
     * setAutoCommit
     *
     * @param autoCommit autoCommit
     * */
    public final void setAutoCommit(boolean autoCommit) {
        try {
            this.connection.setAutoCommit(autoCommit);

        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 提交
     * */
    public final void commit() {
        try {
            this.connection.commit();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 回滚
     * */
    public final void rollback() {
        try {
            this.connection.rollback();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (Exception ignored) {}
    }

}