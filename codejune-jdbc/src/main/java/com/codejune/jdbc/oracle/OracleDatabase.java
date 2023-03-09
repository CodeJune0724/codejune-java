package com.codejune.jdbc.oracle;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * OracleDatabase
 *
 * @author ZJ
 * */
public final class OracleDatabase implements SqlDatabase {

    final OracleJdbc oracleJdbc;

    private final String databaseName;

    OracleDatabase(OracleJdbc oracleJdbc, String databaseName) {
        this.oracleJdbc = oracleJdbc;
        this.databaseName = databaseName;
    }

    @Override
    public String getName() {
        return databaseName;
    }

    @Override
    public OracleTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new InfoException("tableName is null");
        }
        return new OracleTable(this, tableName);
    }

    @Override
    public List<OracleTable> getTables() {
        List<OracleTable> result = new ArrayList<>();
        DatabaseMetaData metaData;
        try {
            metaData = oracleJdbc.getConnection().getMetaData();
        } catch (Exception e) {
            throw new InfoException(e);
        }
        try (ResultSet resultSet = metaData.getTables(databaseName, databaseName.toUpperCase(), null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                result.add(getTable(resultSet.getString("TABLE_NAME")));
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public void deleteTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            return;
        }
        oracleJdbc.execute("DROP TABLE " + tableName);
    }

    @Override
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
            throw new InfoException("建表参数缺失");
        }
        String sql = "CREATE TABLE " + tableName + "(\n";
        sql = StringUtil.append(sql, ArrayUtil.toString(columnList, column -> {
            String result = "\t" + column.getName() + " ";
            result = switch (column.getDataType()) {
                case INT -> result + "NUMBER(" + column.getLength() + ")";
                case STRING -> result + "VARCHAR2(" + column.getLength() + ")";
                case DATE -> result + "DATETIME";
                default -> "\t" + column.getName() + " ";
            };
            if (!column.isNullable()) {
                result = result + " NOT NULL";
            }
            if (column.isPrimaryKey()) {
                result = result + " PRIMARY KEY";
            }
            return result;
        }, ",\n"), "\n)");
        oracleJdbc.execute(sql);
        if (!StringUtil.isEmpty(tableRemark)) {
            oracleJdbc.execute("COMMENT ON TABLE " + tableName + " IS '" + tableRemark + "'");
        }
        for (Column column : columnList) {
            if (!StringUtil.isEmpty(column.getRemark())) {
                oracleJdbc.execute("COMMENT ON COLUMN " + tableName + "." + column.getName() + " IS '" + column.getRemark() + "'");
            }
        }
    }

}