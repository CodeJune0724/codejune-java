package com.codejune.jdbc;

import java.sql.JDBCType;

/**
 * 数据库字段
 *
 * @author ZJ
 * */
public class Column {

    private String name;

    private String remark;

    private JDBCType type;

    private int length;

    private boolean primaryKey;

    private boolean nullable = true;

    private boolean autoincrement;

    public Column() {}

    public Column(String name, JDBCType type) {
        setName(name);
        setType(type);
    }

    public String getName() {
        return name;
    }

    public Column setName(String name) {
        this.name = name;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public Column setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public JDBCType getType() {
        return type;
    }

    public Column setType(JDBCType type) {
        this.type = type;
        return this;
    }

    public int getLength() {
        return length;
    }

    public Column setLength(int length) {
        this.length = length;
        return this;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public Column setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public Column setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public Column setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
        return this;
    }

}