package com.codejune.jdbc;

import com.codejune.core.util.StringUtil;
import java.util.List;

/**
 * 数据库
 *
 * @author ZJ
 * */
public interface Database {

    /**
     * 获取名称
     *
     * @return 名称
     * */
    String getName();

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
     * @return List<? extends Table>
     * */
    List<? extends Table> getTables();

    /**
     * 表是否存在
     *
     * @param tableName 表名
     *
     * @return 表是否存在
     * */
    default boolean existTable(String tableName) {
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

    /**
     * 删除表
     *
     * @param tableName 表名
     * */
    void deleteTable(String tableName);

}