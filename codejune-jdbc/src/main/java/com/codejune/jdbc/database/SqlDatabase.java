package com.codejune.jdbc.database;

import com.codejune.jdbc.Column;
import com.codejune.jdbc.Database;
import java.util.List;

/**
 * SqlDatabase
 *
 * @author ZJ
 * */
public interface SqlDatabase extends Database {

    /**
     * 新建表
     *
     * @param tableName 表名
     * @param tableRemark 表备注
     * @param columnList columnList
     * */
    void createTable(String tableName, String tableRemark, List<Column> columnList);

}