package com.codejune.jdbc.access;

import com.codejune.common.BaseException;
import com.codejune.common.os.File;
import com.codejune.jdbc.oracle.OracleJdbc;
import com.healthmarketscience.jackcess.*;
import com.codejune.jdbc.SqlJdbc;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * AccessDatabaseJdbc
 *
 * @author ZJ
 * */
public class AccessDatabaseJdbc extends SqlJdbc {

    Database database;

    private final java.io.File file;

    OracleJdbc oracleJdbc;

    public AccessDatabaseJdbc(java.io.File file) {
        super(getConnection(file));
        this.file = file;
        reload(false);
        this.oracleJdbc = new OracleJdbc(this.getConnection());
    }

    public AccessDatabaseJdbc(String path) {
        this(new java.io.File(path));
    }

    public AccessDatabaseJdbc(String parent, String path) {
        this(new java.io.File(parent, path));
    }

    final void reload(boolean isReloadConnection) {
        if (isReloadConnection) {
            this.close();
            setConnection(getConnection(this.file));
            this.oracleJdbc.setConnection(this.getConnection());
        }
        try {
            if (this.database != null) {
                try {
                    this.database.close();
                } catch (java.nio.channels.ClosedChannelException ignored) {}
            }
            DatabaseBuilder builder = new DatabaseBuilder(file);
            this.database = builder.open();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    private static void create(java.io.File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
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
    }

    private static Connection getConnection(java.io.File file) {
        create(file);
        try {
            return DriverManager.getConnection("jdbc:ucanaccess://" + file.getAbsolutePath() + ";immediatelyReleaseResources=true;ignoreCase=false");
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public final AccessDatabaseDatabase getDatabase(String databaseName) {
        throw new BaseException("AccessDatabaseJdbc is not found database");
    }

    @Override
    public final List<AccessDatabaseDatabase> getDatabases() {
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
            if (this.database != null) {
                this.database.close();
                this.database = null;
            }
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        }
    }

}