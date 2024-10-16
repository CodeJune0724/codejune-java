package com.codejune.jdbc.sqlite;

import com.codejune.core.BaseException;
import com.codejune.core.util.FileUtil;
import com.codejune.jdbc.SqlJdbc;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * SqliteJdbc
 *
 * @author ZJ
 * */
public class SqliteJdbc extends SqlJdbc {

    public SqliteJdbc(File file) {
        super(getConnection(file));
    }

    @Override
    public SqliteDatabase getDatabase(String databaseName) {
        throw new BaseException("SqliteJdbc is not found database");
    }

    @Override
    public List<SqliteDatabase> getDatabase() {
        throw new BaseException("SqliteJdbc is not found database");
    }

    @Override
    public SqliteDatabase switchDatabase(String databaseName) {
        throw new BaseException("SqliteJdbc is not found database");
    }

    @Override
    public SqliteDatabase getDefaultDatabase() {
        return new SqliteDatabase(this);
    }

    private static Connection getConnection(File file) {
        if (!FileUtil.exist(file)) {
            new com.codejune.core.os.File(file);
        }
        if (!FileUtil.isFile(file)) {
            throw new BaseException("not file");
        }
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}