package com.codejune.service.database;

import com.codejune.Jdbc;
import com.codejune.core.ClassInfo;
import com.codejune.Pool;
import com.codejune.core.util.PackageUtil;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.service.BasePO;
import com.codejune.service.Database;
import jakarta.persistence.Column;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * AccessDatabase
 *
 * @author ZJ
 * */
public class AccessDatabase extends Database {

    @SuppressWarnings("unchecked")
    public AccessDatabase(File databaseFile, String packageName, Class<?> tCLass, Function<AccessDatabaseJdbc, Boolean> check) {
        super(new Pool<>(10) {
            @Override
            public Jdbc create() {
                return new AccessDatabaseJdbc(databaseFile);
            }
            @Override
            public boolean check(Jdbc jdbc) {
                if (check == null) {
                    return true;
                }
                return check.apply((AccessDatabaseJdbc) jdbc);
            }
        });
        List<Class<?>> scan = PackageUtil.scan(packageName, tCLass);
        for (Class<?> c : scan) {
            if (c.toString().startsWith("class") && new ClassInfo(c).isInstanceof(BasePO.class)) {
                Class<? extends BasePO<?>> basePoC = (Class<? extends BasePO<?>>) c;
                String tableName = BasePO.getTableName(basePoC);
                List<com.codejune.jdbc.Column> columnList = new ArrayList<>();
                com.codejune.jdbc.Column column = new com.codejune.jdbc.Column("ID", JDBCType.VARCHAR);
                column.setPrimaryKey(true);
                columnList.add(column);
                List<Field> columnFields = BasePO.getColumnFields(basePoC);
                for (Field field : columnFields) {
                    JDBCType jdbcType;
                    ClassInfo classInfo = new ClassInfo(field.getType());
                    if (classInfo.isInstanceof(Integer.class)) {
                        jdbcType = JDBCType.INTEGER;
                    } else if (classInfo.isInstanceof(Long.class)) {
                        jdbcType = JDBCType.BIGINT;
                    } else if (classInfo.isInstanceof(Double.class)) {
                        jdbcType = JDBCType.DOUBLE;
                    } else if (classInfo.isInstanceof(Boolean.class)) {
                        jdbcType = JDBCType.BOOLEAN;
                    } else if (classInfo.isInstanceof(String.class)) {
                        jdbcType = JDBCType.LONGVARCHAR;
                    } else if (classInfo.isInstanceof(Date.class)) {
                        jdbcType = JDBCType.DATE;
                    } else {
                        throw new Error("sqlType未配置");
                    }
                    column = new com.codejune.jdbc.Column(field.getAnnotation(Column.class).name(), jdbcType);
                    column.setLength(field.getAnnotation(Column.class).length());
                    columnList.add(column);
                }
                try (AccessDatabaseJdbc accessDatabaseJdbc = new AccessDatabaseJdbc(databaseFile)) {
                    accessDatabaseJdbc.getDefaultDatabase().getTable(tableName).reloadTable(columnList);
                }
            }
        }
    }

    public AccessDatabase(File databaseFile, String packageName, Class<?> tCLass) {
        this(databaseFile, packageName, tCLass, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T extends BasePO<ID>, ID> ID getNextId(Jdbc jdbc, Table<T, ID> table) {
        return (ID) UUID.randomUUID().toString();
    }

}