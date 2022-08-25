package com.codejune;

import com.codejune.common.Closeable;
import com.codejune.jdbc.Table;
import com.codejune.common.util.StringUtil;
import java.util.List;

/**
 * Jdbc
 *
 * @author ZJ
 * */
public interface Jdbc extends Closeable {

    /**
     * 获取表
     *
     * @param tableName 表名
     *
     * @return Table
     * */
    Table getTable(String tableName);

    /**
     * 获取表集合
     *
     * @param database 数据库
     *
     * @return Table
     * */
    List<? extends Table> getTables(String database);

    /**
     * 获取表集合
     *
     * @return Table
     * */
    List<? extends Table> getTables();

    /**
     * 表是否存在
     *
     * @param tableName 表名
     *
     * @return 表是否存在
     * */
    default boolean isExistTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return false;
        }
        List<? extends Table> tables = getTables();
        for (Table table : tables) {
            if (tableName.equals(table.getName())) {
                return true;
            }
        }
        return false;
    }

}