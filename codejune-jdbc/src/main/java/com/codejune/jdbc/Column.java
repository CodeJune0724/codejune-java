package com.codejune.jdbc;

import com.codejune.common.DataType;
import java.sql.Types;

/**
 * 数据库字段
 *
 * @author ZJ
 * */
public class Column extends com.codejune.common.model.Column {

    private int sqlType;

    public Column(String name, String remark, DataType dataType, int length, boolean isPrimaryKey) {
        super(name, remark, dataType, length, isPrimaryKey);
        this.sqlType = getSqlType(dataType);
    }

    public Column(String name, String remark, int sqlType, int length, boolean isPrimaryKey) {
        this(name, remark, getDataType(sqlType), length, isPrimaryKey);
    }

    @Override
    public void setDataType(DataType dataType) {
        super.setDataType(dataType);
        this.sqlType = getSqlType(dataType);
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
        this.setDataType(getDataType(sqlType));
    }

    private static int getSqlType(DataType dataType) {
        switch (dataType) {
            case INT:
                return Types.INTEGER;
            case LONG:
                return Types.BIGINT;
            case DOUBLE:
                return Types.DOUBLE;
            case STRING:
                return Types.VARCHAR;
            case LONG_STRING:
                return Types.LONGVARCHAR;
            case BOOLEAN:
                return Types.BIT;
            case DATE:
                return Types.DATE;
        }
        return Types.OTHER;
    }

    private static DataType getDataType(int sqlType) {
        switch (sqlType) {
            case Types.CHAR:
            case Types.VARCHAR:
                return DataType.STRING;
            case Types.LONGVARCHAR:
                return DataType.LONG_STRING;
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.SMALLINT:
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return DataType.DOUBLE;
            case Types.BIT:
                return DataType.BOOLEAN;
            case Types.TINYINT:
            case Types.INTEGER:
                return DataType.INT;
            case Types.BIGINT:
                return DataType.LONG;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return DataType.OBJECT;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return DataType.DATE;
        }
        return DataType.OBJECT;
    }

}