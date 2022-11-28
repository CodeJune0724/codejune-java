package com.codejune.service.database;

import com.codejune.Jdbc;
import com.codejune.common.ClassInfo;
import com.codejune.common.DataType;
import com.codejune.common.Pool;
import com.codejune.common.util.PackageUtil;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.service.BasePO;
import com.codejune.service.Column;
import com.codejune.service.Database;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * AccessDatabase
 *
 * @author ZJ
 * */
public class AccessDatabase extends Database {

    @SuppressWarnings("unchecked")
    public AccessDatabase(File databaseFile, String packageName, Class<?> tCLass) {
        super(new Pool<Jdbc>(10) {
            @Override
            public Jdbc create() {
                return new AccessDatabaseJdbc(databaseFile);
            }
        });
        List<Class<?>> scan = PackageUtil.scan(packageName, tCLass);
        for (Class<?> c : scan) {
            if (c.toString().startsWith("class") && new ClassInfo(c).isInstanceof(BasePO.class)) {
                Class<? extends BasePO<?>> basePoC = (Class<? extends BasePO<?>>) c;
                String tableName = BasePO.getTableName(basePoC);
                List<com.codejune.jdbc.Column> columnList = new ArrayList<>();
                com.codejune.jdbc.Column column = new com.codejune.jdbc.Column(BasePO.getIdName(), DataType.INT);
                column.setAutoincrement(true);
                columnList.add(column);
                List<Field> columnFields = BasePO.getColumnFields(basePoC);
                for (Field field : columnFields) {
                    DataType dataType = DataType.parse(field.getType());
                    if (dataType == DataType.STRING) {
                        dataType = DataType.LONG_STRING;
                    }
                    column = new com.codejune.jdbc.Column(field.getAnnotation(Column.class).name(), dataType);
                    column.setLength(field.getAnnotation(Column.class).length());
                    columnList.add(column);
                }
                AccessDatabaseJdbc accessDatabaseJdbc = new AccessDatabaseJdbc(databaseFile);
                try {
                    accessDatabaseJdbc.getTable(tableName).reloadTable(columnList);
                } finally {
                    accessDatabaseJdbc.close();
                }
            }
        }
    }

}