package com.codejune.jdbc.mysql;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.SqlJdbc;
import com.codejune.jdbc.util.JdbcUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * MysqlJdbc
 *
 * @author ZJ
 * */
public class MysqlJdbc extends SqlJdbc {

    public MysqlJdbc(String host, int port, String database, String username, String password) {
        super(getConnection(host, port, database, username, password));
    }

    public MysqlJdbc(Connection connection) {
        super(connection);
    }

    @Override
    public MysqlTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        return new MysqlTable(this, tableName);
    }

    @Override
    public List<MysqlTable> getTables(String database) {
        if (StringUtil.isEmpty(database)) {
            return null;
        }
        List<MysqlTable> result = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            resultSet = metaData.getTables(database, database, null, new String[]{"TABLE"});
            while (resultSet.next()) {
                String resTableName = resultSet.getString("TABLE_NAME");
                result.add(getTable(database + "." + resTableName));
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            JdbcUtil.close(resultSet);
        }
    }

    @Override
    public List<MysqlTable> getTables() {
        List<MysqlTable> result = new ArrayList<>();
        List<Map<String, Object>> databaseList = queryBySql("SHOW DATABASES");
        for (Map<String, Object> item : databaseList) {
            String database = MapUtil.getValue(item, "Database", String.class);
            List<MysqlTable> tables = getTables(database);
            if (tables == null) {
                continue;
            }
            result.addAll(tables);
        }
        return result;
    }

    private static Connection getConnection(String host, int port, String database, String username, String password) {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            Properties properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            return DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

}