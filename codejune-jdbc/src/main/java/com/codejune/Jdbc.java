package com.codejune;

import com.codejune.common.Closeable;
import com.codejune.jdbc.Database;
import java.util.List;

/**
 * Jdbc
 *
 * @author ZJ
 * */
public interface Jdbc extends Closeable {

    /**
     * 获取数据库
     *
     * @param databaseName 数据库名
     *
     * @return Database
     * */
    Database getDatabase(String databaseName);

    /**
     * 获取数据库集合
     *
     * @return List<Database>
     * */
    List<? extends Database> getDatabases();

    /**
     * 切换数据库
     *
     * @param databaseName 数据库名
     * */
    Database switchDatabase(String databaseName);

    /**
     * 获取默认数据库
     *
     * @return 默认数据库
     * */
    Database getDefaultDatabase();

}