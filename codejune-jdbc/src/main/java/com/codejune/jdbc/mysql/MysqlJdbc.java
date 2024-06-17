package com.codejune.jdbc.mysql;

import com.codejune.core.BaseException;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
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

    private String defaultDatabase = null;

    public MysqlJdbc(Connection connection) {
        super(connection);
        this.oracleJdbc = new OracleJdbc(connection);
    }

    public MysqlJdbc(String host, int port, String database, String username, String password) {
        this(getConnection(host, port, database, username, password));
    }

    private static Connection getConnection(String host, int port, String database, String username, String password) {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + (StringUtil.isEmpty(database) ? "" : database);
            Properties properties = new Properties();
            properties.put("user", username);
            properties.put("password", password);
            return DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public final MysqlDatabase getDatabase(String databaseName) {
        if (StringUtil.isEmpty(databaseName)) {
            throw new BaseException("databaseName is null");
        }
        return new MysqlDatabase(this, databaseName);
    }

    @Override
    public final List<MysqlDatabase> getDatabases() {
        List<MysqlDatabase> result = new ArrayList<>();
        for (Map<String, Object> item : query("SHOW DATABASES")) {
            result.add(getDatabase(MapUtil.get(item, "Database", String.class)));
        }
        return result;
    }

    @Override
    public final MysqlDatabase switchDatabase(String databaseName) {
        if (StringUtil.isEmpty(databaseName)) {
            throw new BaseException("databaseName is null");
        }
        execute("USE " + databaseName);
        this.defaultDatabase = databaseName;
        return getDatabase(databaseName);
    }

    @Override
    public final MysqlDatabase getDefaultDatabase() {
        if (StringUtil.isEmpty(this.defaultDatabase)) {
            List<Map<String, Object>> query = oracleJdbc.query("SELECT database()");
            if (ObjectUtil.isEmpty(query)) {
                throw new BaseException("not query database");
            }
            this.defaultDatabase = MapUtil.get(query.getFirst(), "database()", String.class);
        }
        return getDatabase(this.defaultDatabase);
    }

}