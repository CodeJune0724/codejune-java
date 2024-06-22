package com.codejune.jdbc.oracle;

import com.codejune.core.BaseException;
import com.codejune.jdbc.*;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.StringUtil;
import java.sql.*;
import java.util.*;

/**
 * OracleJdbc
 *
 * @author ZJ
 * */
public class OracleJdbc extends SqlJdbc {

    private String defaultDatabase;

    public OracleJdbc(Connection connection) {
        super(connection);
        try {
            this.defaultDatabase = getConnection().getMetaData().getUserName();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    public OracleJdbc(String host, int port, String sid, String username, String password) {
        this(getConnection(host, port, sid, username, password));
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
        List<Map<String, Object>> query = query("SELECT " + sequence + ".NEXTVAL ID FROM DUAL");
        if (query.isEmpty()) {
            return null;
        }
        if (query.size() != 1) {
            throw new BaseException("查询序列出错");
        }
        return MapUtil.get(query.getFirst(), "ID", Long.class);
    }

    @Override
    public final OracleDatabase getDatabase(String databaseName) {
        if (databaseName == null) {
            throw new BaseException("databaseName is null");
        }
        return new OracleDatabase(this, databaseName);
    }

    @Override
    public final List<OracleDatabase> getDatabase() {
        List<OracleDatabase> result = new ArrayList<>();
        List<Map<String, Object>> users = query("SELECT * FROM ALL_USERS");
        for (Map<String, Object> map : users) {
            result.add(getDatabase(MapUtil.get(map, "USERNAME", String.class)));
        }
        return result;
    }

    @Override
    public final OracleDatabase switchDatabase(String databaseName) {
        if (StringUtil.isEmpty(databaseName)) {
            throw new BaseException("databaseName is null");
        }
        execute("ALTER SESSION SET CURRENT_SCHEMA = " + databaseName);
        this.defaultDatabase = databaseName;
        return getDatabase(databaseName);
    }

    @Override
    public final OracleDatabase getDefaultDatabase() {
        return getDatabase(this.defaultDatabase);
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
            throw new BaseException(e.getMessage());
        }
    }

}