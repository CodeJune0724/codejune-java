package com.codejune.jdbc.oracle;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.*;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.util.JdbcUtil;
import java.sql.*;
import java.util.*;

/**
 * OracleJdbc
 *
 * @author ZJ
 * */
public class OracleJdbc extends SqlJdbc {

    private final Map<String, List<Column>> columnCache = new HashMap<>();

    public OracleJdbc(String host, int port, String sid, String username, String password) {
        super(getConnection(host, port, sid, username, password));
    }

    public OracleJdbc(Connection connection) {
        super(connection);
    }

    @Override
    public final void createTable(String tableName, String tableRemark, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
            throw new InfoException("建表参数缺失");
        }
        String sql = "CREATE TABLE " + tableName + "(\n";
        sql = StringUtil.append(sql, ArrayUtil.toString(columnList, column -> {
            String result = "\t" + column.getName() + " ";
            switch (column.getDataType()) {
                case INT:
                    result = result + "NUMBER(" + column.getLength() + ")";
                    break;
                case STRING:
                    result = result + "VARCHAR2(" + column.getLength() + ")";
                    break;
                case DATE:
                    result = result + "DATETIME";
                    break;
            }
            if (!column.isNullable()) {
                result = result + " NOT NULL";
            }
            if (column.isPrimaryKey()) {
                result = result + " PRIMARY KEY";
            }
            return result;
        }, ",\n"), "\n)");
        execute(sql);
        if (!StringUtil.isEmpty(tableRemark)) {
            execute("COMMENT ON TABLE " + tableName + " IS '" + tableRemark + "'");
        }
        for (Column column : columnList) {
            if (!StringUtil.isEmpty(column.getRemark())) {
                execute("COMMENT ON COLUMN " + tableName + "." + column.getName() + " IS '" + column.getRemark() + "'");
            }
        }
    }

    @Override
    public final OracleTable getTable(String tableName) {
        return new OracleTable(this, tableName);
    }

    @Override
    public final List<OracleTable> getTables(String schema) {
        if (StringUtil.isEmpty(schema)) {
            return null;
        }
        List<OracleTable> result = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            resultSet = metaData.getTables(schema, schema.toUpperCase(), null, new String[]{"TABLE"});
            while (resultSet.next()) {
                String resTableName = resultSet.getString("TABLE_NAME");
                result.add(getTable(schema + "." + resTableName));
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            JdbcUtil.close(resultSet);
        }
    }

    @Override
    public final List<OracleTable> getTables() {
        List<OracleTable> result = new ArrayList<>();
        List<Map<String, Object>> users = queryBySql("SELECT * FROM DBA_USERS");
        for (Map<String, Object> map : users) {
            String username = MapUtil.getValue(map, "USERNAME", String.class);
            List<OracleTable> tables = getTables(username);
            if (tables == null) {
                continue;
            }
            result.addAll(tables);
        }
        return result;
    }

    /**
     * 获取序列的下一个值
     *
     * @param sequence sequence
     *
     * @return 下一个值
     * */
    public final Long getNextSequenceValue(String sequence) {
        if (StringUtil.isEmpty(sequence)) {
            return null;
        }
        List<Map<String, Object>> query = queryBySql("SELECT " + sequence + ".NEXTVAL ID FROM DUAL");
        if (query.size() == 0) {
            return null;
        }
        if (query.size() != 1) {
            throw new InfoException("查询序列出错");
        }
        return MapUtil.getValue(query.get(0), "ID", Long.class);
    }

    /**
     * 缓存字段
     *
     * @param tableName 表名
     * @param columnList 字段集合
     * */
    public final void setColumnCache(String tableName, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || columnList == null) {
            return;
        }
        this.columnCache.put(tableName, columnList);
    }

    /**
     * 获取缓存字段
     *
     * @param tableName 表名
     *
     * @return 缓存字段
     * */
    public final List<Column> getColumnCache(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        return columnCache.get(tableName);
    }

    private static Connection getConnection(String host, int port, String sid, String username, String password) {
        try {
            String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
            Properties properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            properties.put("remarksReporting", "true");
            return DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}