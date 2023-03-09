package com.codejune.jdbc;

import com.codejune.common.DataType;
import java.sql.Types;

/**
 * 数据库字段
 *
 * @author ZJ
 * */
public class Column {

    private String name;

    private String remark;

    private DataType dataType;

    private int length;

    private boolean primaryKey;

    private boolean nullable = true;

    private boolean autoincrement;

    private int sqlType;

    public Column() {}

    public Column(String name, DataType dataType) {
        setName(name);
        setDataType(dataType);
    }

    public Column(String name, int sqlType) {
        setName(name);
        setSqlType(sqlType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    public int getSqlType() {
        return sqlType;
    }

    /**
     * setDataType
     *
     * @param dataType dataType
     * */
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
        this.sqlType = getSqlType(dataType);
    }

    /**
     * setSqlType
     *
     * @param sqlType sqlType
     * */
    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
        this.dataType = getDataType(sqlType);
    }

    private static int getSqlType(DataType dataType) {
        return switch (dataType) {
            case INT -> Types.INTEGER;
            case LONG -> Types.BIGINT;
            case DOUBLE -> Types.DOUBLE;
            case STRING -> Types.VARCHAR;
            case LONG_STRING -> Types.LONGVARCHAR;
            case BOOLEAN -> Types.BIT;
            case DATE -> Types.DATE;
            default -> Types.OTHER;
        };
    }

    private static DataType getDataType(int sqlType) {
        return switch (sqlType) {
            case Types.CHAR, Types.VARCHAR, Types.NVARCHAR -> DataType.STRING;
            case Types.LONGVARCHAR -> DataType.LONG_STRING;
            case Types.NUMERIC, Types.DECIMAL, Types.SMALLINT, Types.REAL, Types.FLOAT, Types.DOUBLE -> DataType.DOUBLE;
            case Types.BIT, Types.BOOLEAN -> DataType.BOOLEAN;
            case Types.TINYINT, Types.INTEGER -> DataType.INT;
            case Types.BIGINT -> DataType.LONG;
            case Types.DATE, Types.TIME, Types.TIMESTAMP -> DataType.DATE;
            default -> DataType.OBJECT;
        };
    }

}