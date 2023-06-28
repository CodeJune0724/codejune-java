package com.codejune.jdbc.util;

import com.codejune.Jdbc;
import com.codejune.common.Charset;
import com.codejune.common.ClassInfo;
import com.codejune.common.classinfo.Field;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import jakarta.persistence.Id;
import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * JdbcUtil
 *
 * @author ZJ
 * */
public final class JdbcUtil {

    /**
     * 通过jpa查询
     *
     * @param <T> T
     * @param jdbc jdbc
     * @param tClass 实体class
     * @param query query
     *
     * @return List
     * */
    public static <T> QueryResult<T> queryByEntity(Jdbc jdbc, Class<T> tClass, Query query) {
        if (jdbc == null || tClass == null) {
            return null;
        }
        if (query == null) {
            query = new Query();
        }
        String tableName = null;
        Table table = tClass.getAnnotation(Table.class);
        if (table != null) {
            tableName = table.name();
        }
        if (StringUtil.isEmpty(tableName)) {
            throw new InfoException(tClass + " not fount @Table");
        }
        Map<String, String> fieldToColumnMap = new HashMap<>();
        Map<String, String> columnToFieldMap = new HashMap<>();
        for (Field field : new ClassInfo(tClass).getFields()) {
            String fieldName = field.getName();
            String columnName;
            if (field.isAnnotation(Id.class)) {
                columnName = StringUtil.humpToUnderline(fieldName);
            } else if (field.isAnnotation(Column.class)) {
                columnName = field.getAnnotation(Column.class).name();
            } else {
                columnName = fieldName;
            }
            fieldToColumnMap.put(fieldName, columnName);
            columnToFieldMap.put(columnName, fieldName);
        }
        query.keyHandler(filed -> {
            String result = MapUtil.getValue(fieldToColumnMap, filed, String.class);
            if (StringUtil.isEmpty(result)) {
                return filed;
            }
            return result;
        });
        return jdbc.getDefaultDatabase().getTable(tableName).query(query).parse(map -> MapUtil.keyHandler(map, column -> {
            String result = MapUtil.getValue(columnToFieldMap, ObjectUtil.toString(column), String.class);
            if (StringUtil.isEmpty(result)) {
                return column;
            }
            return result;
        })).parse(tClass);
    }

    /**
     * 执行脚本
     *
     * @param connection 连接
     * @param scriptFile 脚本文件
     * @param charset 字符集编码
     * */
    public static void executeSqlScript(Connection connection, File scriptFile, Charset charset) {
        if (scriptFile == null) {
            throw new InfoException("scriptFile is null");
        }
        if (charset == null) {
            throw new InfoException("charset is null");
        }
        FileSystemResource fileSystemResource = new FileSystemResource(scriptFile);
        EncodedResource encodedResource = new EncodedResource(fileSystemResource, charset.getName());
        ScriptUtils.executeSqlScript(connection, encodedResource);
    }

    /**
     * 执行脚本
     *
     * @param connection 连接
     * @param scriptFile 脚本文件
     * */
    public static void executeSqlScript(Connection connection, File scriptFile) {
        executeSqlScript(connection, scriptFile, Charset.UTF_8);
    }

}