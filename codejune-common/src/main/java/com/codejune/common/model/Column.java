package com.codejune.common.model;

import com.codejune.common.DataType;
import java.sql.Types;

/**
 * 字段
 *
 * @author  ZJ
 * */
public final class Column {

    private String name;

    private DataType dataType;

    private int length;

    private boolean isPrimaryKey;

    private int sqlType;

    public Column(String name, DataType dataType, int length, boolean isPrimaryKey) {
        this.name = name;
        setDataType(dataType);
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
    }

    public Column(String name, int sqlType, int length, boolean isPrimaryKey) {
        this.name = name;
        setDataType(getDataType(sqlType));
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
        this.sqlType = getSqlType(dataType);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public int getSqlType() {
        return sqlType;
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