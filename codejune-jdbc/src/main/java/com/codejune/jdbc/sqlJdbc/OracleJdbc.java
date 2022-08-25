package com.codejune.jdbc.sqlJdbc;

import com.codejune.common.DataType;
import com.codejune.Jdbc;
import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.jdbc.*;
import com.codejune.jdbc.handler.IdHandler;
import com.codejune.jdbc.table.SqlTable;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.util.SqlUtil;
import com.codejune.common.util.StringUtil;
import java.sql.*;
import java.util.Date;
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
            resultSet = metaData.getTables(null, database.toUpperCase(), null, new String[]{"TABLE"});
            while (resultSet.next()) {
                String resTableName = resultSet.getString("TABLE_NAME");
                result.add(getTable(database + "." + resTableName));
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            close(resultSet);
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
            properties.put("remarksReporting","true");
            return DriverManager.getConnection(url, properties);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    /**
     * OracleTable
     *
     * @author ZJ
     * */
    public static final class OracleTable implements SqlTable {

        private final OracleJdbc oracleJdbc;

        private final String tableName;

        private OracleTable(OracleJdbc oracleJdbc, String tableName) {
            this.oracleJdbc = oracleJdbc;
            this.tableName = tableName;
        }

        /**
         * 新增
         *
         * @param data 数据
         * @param idHandler idHandler
         *
         * @return 受影响的行数
         * */
        public long insert(List<Map<String, Object>> data, IdHandler idHandler) {
            if (data.size() == 0) {
                return 0;
            }

            List<Column> allColumn = getColumns();
            if (ObjectUtil.isEmpty(allColumn)) {
                return 0;
            }

            if (idHandler == null) {
                idHandler = new IdHandler() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getSequence() {
                        return null;
                    }
                };
            }

            Connection connection = oracleJdbc.getConnection();

            // 序列是有生效
            boolean isSequence = !StringUtil.isEmpty(idHandler.getName()) && !StringUtil.isEmpty(idHandler.getSequence());

            String sql = "INSERT INTO " + tableName + " (";
            String value = " VALUES (";
            int index = 0;
            for (Column column : allColumn) {
                index++;
                String end;

                if (index == allColumn.size()) {
                    end = ")";
                } else {
                    end = ", ";
                }

                sql = StringUtil.append(sql, column.getName(), end);
                if (isSequence && column.getName().equals(idHandler.getName())) {
                    value = StringUtil.append(value, idHandler.getSequence(), end);
                } else {
                    value = StringUtil.append(value, "?", end);
                }
            }
            sql = StringUtil.append(sql, value);

            PreparedStatement preparedStatement = null;
            try {
                connection.setAutoCommit(false);
                preparedStatement = connection.prepareStatement(sql);
                int dataSize = data.size();
                for (int i = 0; i < dataSize; i++) {
                    Map<String, Object> map = data.get(i);
                    index = 0;
                    for (Column column : allColumn) {
                        if (isSequence && column.getName().equals(idHandler.getName())) {
                            continue;
                        }
                        index++;
                        Object filedData = map.get(column.getName());
                        filedData = ObjectUtil.subString(filedData, column.getLength());
                        if (filedData == null) {
                            preparedStatement.setNull(index, column.getSqlType());
                        } else if (column.getDataType() == DataType.DATE) {
                            preparedStatement.setTimestamp(index, new Timestamp(((Date) DataType.transform(filedData, column.getDataType())).getTime()));
                        } else if (column.getDataType() == DataType.OBJECT) {
                            preparedStatement.setObject(index, filedData);
                        } else {
                            preparedStatement.setObject(index, DataType.transform(filedData, column.getDataType()));
                        }
                    }
                    preparedStatement.addBatch();
                    if (i != 0 && i % 50000 == 0) {
                        preparedStatement.executeBatch();
                        connection.commit();
                        preparedStatement.clearBatch();
                    }
                }
                preparedStatement.executeBatch();
                connection.commit();
                return dataSize;
            } catch (SQLException e) {
                throw new InfoException(e.getMessage() + ": " + sql);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                close(preparedStatement);
            }
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
            List<Map<String, Object>> query = this.oracleJdbc.queryBySql("SELECT " + getName() + "." + sequence + ".NEXTVAL ID FROM DUAL");
            if (query.size() == 0) {
                return null;
            }
            if (query.size() != 1) {
                throw new InfoException("查询序列出错");
            }
            return MapUtil.getValue(query.get(0), "ID", Long.class);
        }

        @Override
        public List<Column> getColumns() {
            return oracleJdbc.getColumns(this.tableName);
        }

        @Override
        public String getRemark() {
            ResultSet resultSet = null;
            try {
                DatabaseMetaData metaData = this.oracleJdbc.getConnection().getMetaData();
                String schema;
                String originTableName;
                if (this.tableName.contains(".")) {
                    String[] split = this.tableName.split("\\.");
                    schema = split[0];
                    originTableName = split[1];
                } else {
                    schema = null;
                    originTableName = this.tableName;
                }
                resultSet = metaData.getTables(null, schema == null ? null : schema.toUpperCase(), originTableName, new String[]{"TABLE", "REMARKS"});
                while (resultSet.next()) {
                    return resultSet.getString("REMARKS");
                }
                return null;
            } catch (Exception e) {
                throw new InfoException(e);
            } finally {
                close(resultSet);
            }
        }

        @Override
        public String getName() {
            return tableName;
        }

        @Override
        public long insert(List<Map<String, Object>> data) {
            return insert(data, null);
        }

        @Override
        public long delete(Filter filter) {
            if (StringUtil.isEmpty(tableName)) {
                throw new InfoException("表名不能为空");
            }
            if (filter != null) {
                filter.filter(getColumns());
            }
            String deleteSql = "DELETE FROM " + tableName + " " + SqlUtil.toWhere(filter);
            return oracleJdbc.execute(deleteSql);
        }

        @Override
        public long update(Filter filter, Map<String, Object> setData) {
            if (StringUtil.isEmpty(tableName)) {
                throw new InfoException("表名不能为空");
            }
            if (setData == null) {
                setData = new HashMap<>();
            }
            if (filter == null) {
                filter = new Filter();
            }

            filter.filter(getColumns());

            // 根据字段类型转换数据
            Set<String> keySet = setData.keySet();
            List<Column> columnList = getColumns();
            if (columnList == null) {
                columnList = new ArrayList<>();
            }
            for (String key : keySet) {
                Object value = setData.get(key);
                Column column = null;
                for (Column item : columnList) {
                    if (item.getName().equals(key)) {
                        column = item;
                        break;
                    }
                }
                DataType dataType = null;
                if (column != null) {
                    dataType = column.getDataType();
                }
                value = DataType.transform(value, dataType);
                setData.put(key, value);
            }

            String updateSql = SqlUtil.parseUpdateSql(tableName, filter, setData);
            return oracleJdbc.execute(updateSql);
        }

        @Override
        public QueryResult<Map<String, Object>> query(Query query) {
            return query(query, null);
        }

        QueryResult<Map<String, Object>> query(Query query, Class<? extends Jdbc> jdbcType) {
            if (query == null) {
                query = new Query();
            }

            QueryResult<Map<String, Object>> queryResult = new QueryResult<>();
            String sql = "SELECT * FROM " + tableName;

            // 数据过滤
            Filter filter = query.filter();
            filter.filter(getColumns());
            sql = sql + " " + SqlUtil.toWhere(filter, jdbcType);

            // count
            String countSql = StringUtil.append("SELECT COUNT(*) C FROM (", sql, ")");
            List<Map<String, Object>> countData = oracleJdbc.queryBySql(countSql);
            queryResult.setCount(Long.parseLong(countData.get(0).get("C").toString()));

            // 排序
            if (query.isSort()) {
                sql = StringUtil.append(sql, " ORDER BY ", ArrayUtil.toString(query.sort(), sort -> sort.getColumn() + " " + sort.getOrderBy().name(), ", "));
            }

            // 分页查询
            if (query.isPage()) {
                Integer page = query.getPage();
                Integer size = query.getSize();
                sql = StringUtil.append("SELECT ROWNUM R, T.* FROM (", sql, ") T");
                sql = StringUtil.append("SELECT * FROM (SELECT T.* FROM (", sql, ") T WHERE R <= ", (page * size) + "", ") WHERE R >= ", (size * (page - 1) + 1) + "");
            }

            List<String> field = new ArrayList<>();
            field.add("R");
            List<Map<String, Object>> data = oracleJdbc.queryBySql(sql, field);
            queryResult.setData(data);

            return queryResult;
        }

    }

}