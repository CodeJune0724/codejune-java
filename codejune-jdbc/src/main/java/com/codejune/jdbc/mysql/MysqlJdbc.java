package com.codejune.jdbc.mysql;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.SqlJdbc;
import com.codejune.jdbc.oracle.OracleJdbc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * MysqlJdbc
 *
 * @author ZJ
 * */
public class MysqlJdbc extends SqlJdbc {

    final OracleJdbc oracleJdbc;

    public MysqlJdbc(Connection connection) {
        super(connection);
        this.oracleJdbc = new OracleJdbc(connection);
    }

    public MysqlJdbc(String host, int port, String database, String username, String password) {
        this(getConnection(host, port, database, username, password));
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

    @Override
    public final MysqlDatabase getDatabase(String databaseName) {
        if (StringUtil.isEmpty(databaseName)) {
            throw new InfoException("databaseName is null");
        }
        return new MysqlDatabase(this, databaseName);
    }

    @Override
    public final List<MysqlDatabase> getDatabases() {
        List<MysqlDatabase> result = new ArrayList<>();
        for (Map<String, Object> item : query("SHOW DATABASES")) {
            result.add(getDatabase(MapUtil.getValue(item, "Database", String.class)));
        }
        return result;
    }

    @Override
    public final MysqlDatabase switchDatabase(String databaseName) {
        if (StringUtil.isEmpty(databaseName)) {
            throw new InfoException("databaseName is null");
        }
        execute("USE " + databaseName);
        return getDatabase(databaseName);
    }

    @Override
    public final MysqlDatabase getDefaultDatabase() {
        return getDatabase(oracleJdbc.getDefaultDatabase().getName());
    }

}