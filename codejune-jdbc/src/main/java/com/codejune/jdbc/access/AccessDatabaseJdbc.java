package com.codejune.jdbc.access;

import com.codejune.core.BaseException;
import com.codejune.core.os.File;
import com.codejune.core.util.FileUtil;
import com.codejune.jdbc.oracle.OracleJdbc;
import com.healthmarketscience.jackcess.*;
import com.codejune.jdbc.SqlJdbc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * AccessDatabaseJdbc
 *
 * @author ZJ
 * */
public class AccessDatabaseJdbc extends SqlJdbc {

    protected Database database;

    private final java.io.File file;

    protected OracleJdbc oracleJdbc;

    public AccessDatabaseJdbc(java.io.File file) {
        super(getConnection(file));
        this.file = file;
        reload(false);
    }

    public AccessDatabaseJdbc(String path) {
        this(new java.io.File(path));
    }

    public AccessDatabaseJdbc(String parent, String path) {
        this(new java.io.File(parent, path));
    }

    @Override
    public final AccessDatabaseDatabase getDatabase(String databaseName) {
        throw new BaseException("AccessDatabaseJdbc is not found database");
    }

    @Override
    public final List<AccessDatabaseDatabase> getDatabase() {
        throw new BaseException("AccessDatabaseJdbc is not found database");
    }

    @Override
    public final AccessDatabaseDatabase switchDatabase(String databaseName) {
        throw new BaseException("AccessDatabaseJdbc is not found database");
    }

    @Override
    public final AccessDatabaseDatabase getDefaultDatabase() {
        return new AccessDatabaseDatabase(this);
    }

    @Override
    public final void close() {
        super.close();
        try {
            this.database.close();
        } catch (Exception ignored) {}
        oracleJdbc.close();
    }

    final void reload(boolean reloadConnection) {
        if (reloadConnection) {
            this.close();
            setConnection(getConnection(file));
        }
        try {
            this.database.close();
        } catch (Exception ignored) {}
        try {
            database = new DatabaseBuilder(file).open();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
        oracleJdbc = new OracleJdbc(getConnection());
    }

    private static Connection getConnection(java.io.File file) {
        if (file == null) {
            throw new BaseException("file is null");
        }
        if (!FileUtil.exist(file)) {
            String name = file.getName();
            Database.FileFormat fileFormat;
            if (name.contains(".")) {
                String[] split = name.split("\\.");
                String suffix = split[split.length - 1];
                suffix = suffix.toUpperCase();
                fileFormat = switch (suffix) {
                    case "MDB" -> Database.FileFormat.V2003;
                    case "ACCDB" -> Database.FileFormat.V2016;
                    default -> throw new BaseException("类型错误");
                };
            } else {
                throw new BaseException("文件名错误");
            }
            new File(file);
            try {
                Database database = DatabaseBuilder.create(fileFormat, file);
                if (database != null) {
                    database.close();
                }
            } catch (Exception e) {
                throw new BaseException(e.getMessage());
            }
        }
        try {
            return DriverManager.getConnection("jdbc:ucanaccess://" + file.getAbsolutePath() + ";ignoreCase=false;immediatelyReleaseResources=true;");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}