package com.codejune.jdbc.util;

import com.codejune.Jdbc;
import com.codejune.common.Charset;
import com.codejune.common.ClassInfo;
import com.codejune.common.classInfo.Field;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.jdbc.handler.ColumnToFieldHandler;
import com.codejune.jdbc.handler.FieldToColumnHandler;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import javax.persistence.Id;
import java.io.File;
import java.sql.Connection;
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
     * @param jpaRepository jpaRepository
     * @param query query
     *
     * @return List
     * */
    @SuppressWarnings("unchecked")
    public static <T> QueryResult<T> queryByJpa(Jdbc jdbc, Class<? extends JpaRepository<T, ?>> jpaRepository, Query query) {
        if (jdbc == null || jpaRepository == null) {
            return null;
        }
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
        FieldToColumnHandler entityKeyHandler = new FieldToColumnHandler(aClass, idName);
        query.keyHandler(entityKeyHandler);
        QueryResult<Map<String, Object>> queryResult = jdbc.getDefaultDatabase().getTable(tableName).query(query);
        ColumnToFieldHandler columnToFieldHandler = new ColumnToFieldHandler(aClass, idName);
        return (QueryResult<T>) queryResult.parse(aClass, key -> columnToFieldHandler.handler(ObjectUtil.toString(key)));
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
        EncodedResource encodedResource = new EncodedResource(fileSystemResource, charset.name());
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