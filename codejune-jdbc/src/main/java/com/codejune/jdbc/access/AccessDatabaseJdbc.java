package com.codejune.jdbc.access;

import com.codejune.common.os.File;
import com.healthmarketscience.jackcess.*;
import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.SqlJdbc;
import com.codejune.common.util.StringUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * AccessDatabaseJdbc
 *
 * @author ZJ
 * */
public class AccessDatabaseJdbc extends SqlJdbc {

    protected Database database;

    private final java.io.File file;

    public AccessDatabaseJdbc(java.io.File file) {
        super(getConnection(file));
        this.file = file;
        reload(false);
    }

    @Override
    public AccessDatabaseTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        return new AccessDatabaseTable(this, tableName);
    }

    @Override
    public List<AccessDatabaseTable> getTables() {
        try {
            List<AccessDatabaseTable> result = new ArrayList<>();
            for (String tableName : this.database.getTableNames()) {
                if (tableName.startsWith("~")) {
                    continue;
                }
                result.add(getTable(tableName));
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public List<AccessDatabaseTable> getTables(String database) {
        return getTables();
    }

    @Override
    public void close() {
        super.close();
        try {
            if (this.database != null) {
                this.database.close();
                this.database = null;
            }
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        }
    }

    void reload(boolean isReloadConnection) {
        if (isReloadConnection) {
            this.close();
            setConnection(getConnection(this.file));
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
            throw new InfoException(e.getMessage());
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
                switch (suffix) {
                    case "MDB":
                        fileFormat = Database.FileFormat.V2003;
                        break;
                    case "ACCDB":
                        fileFormat = Database.FileFormat.V2016;
                        break;
                    default:
                        throw new InfoException("类型错误");
                }
            } else {
                throw new InfoException("文件名错误");
            }
            new File(file);
            try {
                Database database = DatabaseBuilder.create(fileFormat, file);
                if (database != null) {
                    database.close();
                }
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
        }
    }

    private static Connection getConnection(java.io.File file) {
        create(file);
        try {
            return DriverManager.getConnection("jdbc:ucanaccess://" + file.getAbsolutePath() + ";immediatelyReleaseResources=true");
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}