package com.codejune.jdbc.sqlJdbc;

import com.codejune.common.os.File;
import com.codejune.common.util.ArrayUtil;
import com.healthmarketscience.jackcess.*;
import com.codejune.common.DataType;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.SqlJdbc;
import com.codejune.jdbc.table.SqlTable;
import com.codejune.jdbc.Filter;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.QueryResult;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.codejune.jdbc.Column;

/**
 * AccessDatabaseJdbc
 *
 * @author ZJ
 * */
public class AccessDatabaseJdbc extends SqlJdbc {

    private Database database;

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

    private void reload(boolean isReloadConnection) {
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

    /**
     * AccessDatabaseTable
     *
     * @author ZJ
     * */
    public static final class AccessDatabaseTable implements SqlTable {

        private final AccessDatabaseJdbc accessDatabaseJdbc;

        private final String tableName;

        private static final Object OBJECT = new Object();

        private AccessDatabaseTable(AccessDatabaseJdbc accessDatabaseJdbc, String tableName) {
            this.accessDatabaseJdbc = accessDatabaseJdbc;
            this.tableName = tableName;
        }

        /**
         * 重新加载表信息
         *
         * @param columnList 字段
         * */
        public void reloadTable(List<Column> columnList) {
            try {
                if (ObjectUtil.isEmpty(columnList)) {
                    throw new InfoException("字段不能为空");
                }

                com.healthmarketscience.jackcess.Table table = accessDatabaseJdbc.database.getTable(tableName);

                // 所有字段
                List<ColumnBuilder> columnBuilderList = new ArrayList<>();
                int p = 0;
                int index = 0;
                for (Column column : columnList) {
                    ColumnBuilder columnBuilder = new ColumnBuilder(column.getName());
                    DataType dataType = column.getDataType();
                    if (column.isPrimaryKey()) {
                        columnBuilder.setAutoNumber(true).setSQLType(Types.BIGINT);
                        p = index;
                    } else if (dataType == DataType.INT) {
                        columnBuilder.setSQLType(Types.INTEGER);
                    } else if (dataType == DataType.STRING) {
                        columnBuilder.setSQLType(Types.VARCHAR);
                    } else if (dataType == DataType.LONG_STRING) {
                        columnBuilder.setSQLType(Types.LONGVARCHAR);
                    } else if (dataType == DataType.DATE) {
                        columnBuilder.setSQLType(Types.DATE);
                    } else if (dataType == DataType.BOOLEAN) {
                        columnBuilder.setSQLType(Types.BOOLEAN);
                    } else {
                        throw new ErrorException(dataType + "未配置");
                    }
                    columnBuilderList.add(columnBuilder);
                    index++;
                }
                ArrayUtil.move(columnBuilderList, p, 0);

                // 获取表数据
                List<Map<String, Object>> tableData;
                if (table == null) {
                    tableData = new ArrayList<>();
                } else {
                    tableData = query().getData();
                    accessDatabaseJdbc.execute("DROP TABLE " + tableName);
                    accessDatabaseJdbc.reload(true);
                }

                // 建表
                TableBuilder tableBuilder = new TableBuilder(tableName);
                for (ColumnBuilder columnBuilder : columnBuilderList) {
                    tableBuilder.addColumn(columnBuilder);
                }
                tableBuilder.toTable(accessDatabaseJdbc.database);
                accessDatabaseJdbc.reload(true);

                // 保存数据
                this.insert(tableData);
            } catch (IOException e) {
                throw new ErrorException(e.getMessage());
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
        }

        /**
         * 查询
         *
         * @param query query
         * @param isCase 时候区分大小写
         *
         * @return QueryResult
         * */
        public QueryResult<Map<String, Object>> query(Query query, boolean isCase) {
            OracleJdbc.OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
            return table.query(query, isCase ? AccessDatabaseJdbc.class : null);
        }

        @Override
        public List<Column> getColumns() {
            List<Column> result = new ArrayList<>();
            List<? extends com.healthmarketscience.jackcess.Column> columns;
            try {
                columns = this.accessDatabaseJdbc.database.getTable(tableName).getColumns();
            } catch (Exception e) {
                throw new InfoException(e);
            }
            for (com.healthmarketscience.jackcess.Column column : columns) {
                String name = column.getName();
                int sqlType;
                int length = column.getLength();
                boolean isPrimaryKey = column.isAutoNumber();
                try {
                    sqlType = column.getSQLType();
                } catch (Exception e) {
                    throw new InfoException(e);
                }

                result.add(new Column(name, null, sqlType, length, isPrimaryKey));
            }
            return result;
        }

        @Override
        public String getRemark() {
            return new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName).getRemark();
        }

        @Override
        public String getName() {
            return tableName;
        }

        @Override
        public long insert(List<Map<String, Object>> data) {
            OracleJdbc.OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
            if (table == null) {
                return 0;
            }
            synchronized (OBJECT) {
                return table.insert(data);
            }
        }

        @Override
        public long delete(Filter filter) {
            OracleJdbc.OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
            if (table == null) {
                return 0;
            }
            return table.delete(filter);
        }

        @Override
        public long update(Filter filter, Map<String, Object> setData) {
            OracleJdbc.OracleTable table = new OracleJdbc(accessDatabaseJdbc.getConnection()).getTable(tableName);
            if (table == null) {
                return 0;
            }
            return table.update(filter, setData);
        }

        @Override
        public QueryResult<Map<String, Object>> query(Query query) {
            return query(query, true);
        }

        @Override
        public QueryResult<Map<String, Object>> query() {
            return query(null);
        }

    }

}
