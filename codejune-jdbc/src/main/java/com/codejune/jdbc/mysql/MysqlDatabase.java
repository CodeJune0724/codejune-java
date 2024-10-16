package com.codejune.jdbc.mysql;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.Column;
import com.codejune.jdbc.database.SqlDatabase;
import com.codejune.jdbc.table.SqlTable;
import java.util.ArrayList;
import java.util.List;

/**
 * MysqlDatabase
 *
 * @author ZJ
 * */
public final class MysqlDatabase extends SqlDatabase {

    MysqlDatabase(MysqlJdbc mysqlJdbc, String name) {
        super(mysqlJdbc, name);
    }

    @Override
    public MysqlJdbc getJdbc() {
        return (MysqlJdbc) super.getJdbc();
    }

    @Override
    public MysqlTable getTable(String tableName) {
        return new MysqlTable(this, tableName);
    }

    @Override
    public List<MysqlTable> getTable() {
        List<MysqlTable> result = new ArrayList<>();
        for (SqlTable sqlTable : super.getTable()) {
            result.add(getTable(sqlTable.getName()));
        }
        return result;
    }

    @Override
    public void createTable(String tableName, String tableRemark, List<Column> columnList) {
        if (StringUtil.isEmpty(tableName) || ObjectUtil.isEmpty(columnList)) {
            throw new BaseException("建表参数缺失");
        }
        String sql = "CREATE TABLE " + tableName + " (\n";
        sql = StringUtil.append(sql, ArrayUtil.toString(columnList, column -> {
            String result = "\t" + column.getName() + " ";
            result = switch (column.getType()) {
                case INTEGER -> result + "INT";
                case VARCHAR -> result + "VARCHAR(" + column.getLength() + ")";
                case DATE -> result + "DATETIME";
                case DOUBLE -> result + "DOUBLE";
                default -> throw new Error("column.getDataType()未配置");
            };
            if (!column.isNullable()) {
                result = result + " NOT NULL";
            }
            if (column.isPrimaryKey()) {
                result = result + " PRIMARY KEY";
            }
            if (column.isAutoincrement()) {
                result = result + " AUTO_INCREMENT";
            }
            if (!StringUtil.isEmpty(column.getRemark())) {
                result = result + " COMMENT '" + column.getRemark() + "'";
            }
            return result;
        }, ",\n"), "\n)");
        if (!StringUtil.isEmpty(tableRemark)) {
            sql = StringUtil.append(sql, " COMMENT = '" + tableRemark + "'");
        }
        this.getJdbc().execute(sql);
    }

}