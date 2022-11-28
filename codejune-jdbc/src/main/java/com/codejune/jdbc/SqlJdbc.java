package com.codejune.jdbc;

import com.codejune.common.ClassInfo;
import com.codejune.Jdbc;
import com.codejune.common.exception.InfoException;
import com.codejune.common.Charset;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.handler.ColumnToFieldKeyHandler;
import com.codejune.jdbc.handler.FieldToColumnKeyHandler;
import com.codejune.jdbc.util.JdbcUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.io.File;
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
        this.setConnection(connection);
    }

    public Connection getConnection() {
        return connection;
    }

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
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.connection.prepareStatement(sql);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new InfoException(e.getMessage());
        } finally {
            JdbcUtil.close(preparedStatement);
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
    public final List<Map<String, Object>> queryBySql(String sql, List<String> filterFields) {
        List<Map<String, Object>> result = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = this.connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(resultSetMetaData.getColumnName(i));
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
            throw new InfoException(e.getMessage() + "：" + sql);
        } finally {
            JdbcUtil.close(resultSet);
            JdbcUtil.close(preparedStatement);
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
    public final List<Map<String, Object>> queryBySql(String sql) {
        return queryBySql(sql, null);
    }

    /**
     * 通过jpa查询
     *
     * @param <T> T
     * @param jpaRepository jpaRepository
     * @param query query
     *
     * @return List
     * */
    @SuppressWarnings("unchecked")
    public final <T> QueryResult<T> queryByJpa(Class<? extends JpaRepository<T, ?>> jpaRepository, Query query) {
        if (query == null) {
            query = new Query();
        }

        ClassInfo classInfo = new ClassInfo(jpaRepository);
        ClassInfo superClass = classInfo.getSuperClass(JpaRepository.class);
        if (superClass == null || !superClass.existsGenericClass()) {
            throw new InfoException("jpaRepository传入错误");
        }
        ClassInfo TCLass = superClass.getGenericClass().get(0);
        Class<?> aClass = TCLass.getOriginClass();
        javax.persistence.Table table = aClass.getAnnotation(javax.persistence.Table.class);
        if (table == null) {
            throw new InfoException("实体类未配置表名");
        }
        String tableName = table.name();

        // 获取主键名
        String idName = "ID";
        List<Column> columns = getColumns(tableName);
        if (columns == null) {
            columns = new ArrayList<>();
        }
        for (Column column : columns) {
            if (column.isPrimaryKey()) {
                idName = column.getName();
                break;
            }
        }

        // 设置key处理
        FieldToColumnKeyHandler entityKeyHandler = new FieldToColumnKeyHandler(aClass, idName);
        query.setKeyHandler(entityKeyHandler);
        QueryResult<Map<String, Object>> queryResult = getTable(tableName).query(query);
        return (QueryResult<T>) queryResult.parse(aClass, new ColumnToFieldKeyHandler(aClass, idName));
    }

    /**
     * 执行脚本
     *
     * @param scriptFile 脚本文件
     * @param charset 字符集编码
     * */
    public final void executeSqlScript(File scriptFile, Charset charset) {
        if (scriptFile == null) {
            throw new InfoException("scriptFile is null");
        }
        if (charset == null) {
            throw new InfoException("charset is null");
        }
        FileSystemResource fileSystemResource = new FileSystemResource(scriptFile);
        EncodedResource encodedResource = new EncodedResource(fileSystemResource, charset.name());
        ScriptUtils.executeSqlScript(connection, encodedResource);
    }

    /**
     * 执行脚本
     *
     * @param scriptFile 脚本文件
     * */
    public final void executeSqlScript(File scriptFile) {
        this.executeSqlScript(scriptFile, Charset.UTF_8);
    }

    /**
     * 获取字段
     *
     * @param tableName 表名
     *
     * @return 所有字段
     * */
    public final List<Column> getColumns(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        List<Column> result = new ArrayList<>();

        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = connection.getMetaData();
        } catch (Exception e) {
            throw new InfoException(e);
        }

        String schema;
        String originTableName;
        try {
            if (tableName.contains(".")) {
                schema = tableName.split("\\.")[0];
                originTableName = tableName.substring(tableName.indexOf(".") + 1);
            } else {
                schema = databaseMetaData.getUserName();
                originTableName = tableName;
            }
            if (StringUtil.isEmpty(schema)) {
                originTableName = null;
            }
        } catch (Exception e) {
            throw new InfoException(e);
        }

        // 获取主键
        ResultSet primaryKeyResultSet = null;
        List<String> primaryKeyList = new ArrayList<>();
        try {
            primaryKeyResultSet = databaseMetaData.getPrimaryKeys(connection.getCatalog(), schema, originTableName);
            while (primaryKeyResultSet.next()) {
                primaryKeyList.add(primaryKeyResultSet.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            JdbcUtil.close(primaryKeyResultSet);
        }

        // 获取字段
        ResultSet columnResultSet = null;
        try {
            columnResultSet = databaseMetaData.getColumns(connection.getCatalog(), schema, originTableName, null);
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

    /**
     * 新建表
     *
     * @param tableName 表名
     * @param tableRemark 表备注
     * @param columnList columnList
     * */
    public abstract void createTable(String tableName, String tableRemark, List<Column> columnList);

    /**
     * 删除表
     *
     * @param tableName 表名
     * */
    public void deleteTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return;
        }
        execute("DROP TABLE " + tableName);
    }

    /**
     * 获取schema
     *
     * @return schema
     * */
    public final String getSchema() {
        try {
            return connection.getMetaData().getUserName();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public void close() {
        if (this.connection == null) {
            return;
        }
        try {
            this.connection.close();
            this.connection = null;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}