package com.codejune.jdbc;

import com.codejune.common.ClassInfo;
import com.codejune.Jdbc;
import com.codejune.common.classInfo.Field;
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
import javax.persistence.Id;
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
    public final List<Map<String, Object>> query(String sql, List<String> filterFields) {
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
    public final List<Map<String, Object>> query(String sql) {
        return query(sql, null);
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
        ClassInfo superClass = new ClassInfo(jpaRepository).getSuperClass(JpaRepository.class);
        if (superClass == null || !superClass.existsGenericClass()) {
            throw new InfoException("jpaRepository传入错误");
        }
        Class<?> aClass = superClass.getGenericClass().get(0).getOriginClass();
        javax.persistence.Table tableAnnotation = aClass.getAnnotation(javax.persistence.Table.class);
        if (tableAnnotation == null) {
            throw new InfoException("实体类未配置表名");
        }
        String tableName = tableAnnotation.name();
        String idName = "ID";
        for (Field field : new ClassInfo(aClass).getFields()) {
            Id idAnnotation = field.getAnnotation(Id.class);
            if (idAnnotation != null) {
                idName = StringUtil.humpToUnderline(field.getName());
                break;
            }
        }
        FieldToColumnKeyHandler entityKeyHandler = new FieldToColumnKeyHandler(aClass, idName);
        query.setKeyHandler(entityKeyHandler);
        QueryResult<Map<String, Object>> queryResult = getDefaultDatabase().getTable(tableName).query(query);
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