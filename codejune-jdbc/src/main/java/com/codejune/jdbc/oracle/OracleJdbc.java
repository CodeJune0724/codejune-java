package com.codejune.jdbc.oracle;

import com.codejune.common.exception.InfoException;
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

    public OracleJdbc(String host, int port, String sid, String username, String password) {
        super(getConnection(host, port, sid, username, password));
    }

    public OracleJdbc(Connection connection) {
        super(connection);
    }

    /**
     * 获取序列的下一个值
     *
     * @param sequence sequence
     *
     * @return 下一个值
     * */
    public Long getNextSequenceValue(String sequence) {
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

    @Override
    public OracleTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        return new OracleTable(this, tableName);
    }

    @Override
    public List<OracleTable> getTables(String database) {
        if (StringUtil.isEmpty(database)) {
            return null;
        }
        List<OracleTable> result = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            resultSet = metaData.getTables(database, database.toUpperCase(), null, new String[]{"TABLE"});
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
    public List<OracleTable> getTables() {
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