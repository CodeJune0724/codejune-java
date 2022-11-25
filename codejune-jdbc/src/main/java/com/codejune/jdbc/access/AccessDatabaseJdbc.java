package com.codejune.jdbc.access;

import com.codejune.common.DataType;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.os.File;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.oracle.OracleJdbc;
import com.healthmarketscience.jackcess.*;
import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.SqlJdbc;
import com.codejune.common.util.StringUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
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

    protected OracleJdbc oracleJdbc;

    public AccessDatabaseJdbc(java.io.File file) {
        super(getConnection(file));
        this.file = file;
        reload(false);
        this.oracleJdbc = new OracleJdbc(this.getConnection());
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
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        try {
            if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
                throw new InfoException("建表参数缺失");
            }
            com.healthmarketscience.jackcess.Table table = database.getTable(tableName);
            if (table != null) {
                throw new InfoException(tableName + "表已存在");
            }
            List<ColumnBuilder> columnBuilderList = new ArrayList<>();
            for (Column column : columnList) {
                ColumnBuilder columnBuilder = new ColumnBuilder(column.getName());
                DataType dataType = column.getDataType();
                if (column.isAutoincrement()) {
                    columnBuilder.setAutoNumber(true);
                    dataType = DataType.LONG;
                }
                if (dataType == com.codejune.common.DataType.INT) {
                    columnBuilder.setSQLType(Types.INTEGER);
                } else if (dataType == DataType.LONG) {
                    columnBuilder.setSQLType(Types.BIGINT);
                }else if (dataType == com.codejune.common.DataType.STRING) {
                    columnBuilder.setSQLType(Types.VARCHAR);
                } else if (dataType == com.codejune.common.DataType.LONG_STRING) {
                    columnBuilder.setSQLType(Types.LONGVARCHAR);
                } else if (dataType == com.codejune.common.DataType.DATE) {
                    columnBuilder.setSQLType(Types.DATE);
                } else if (dataType == DataType.BOOLEAN) {
                    columnBuilder.setSQLType(Types.BOOLEAN);
                } else {
                    throw new ErrorException(dataType + "未配置");
                }
                columnBuilderList.add(columnBuilder);
            }
            TableBuilder tableBuilder = new TableBuilder(tableName);
            for (ColumnBuilder columnBuilder : columnBuilderList) {
                tableBuilder.addColumn(columnBuilder);
            }
            tableBuilder.toTable(database);
            reload(true);
        } catch (IOException e) {
            throw new ErrorException(e.getMessage());
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public void deleteTable(String tableName) {
        super.deleteTable(tableName);
        reload(true);
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