package com.codejune.common.model;

import com.codejune.common.DataType;

/**
 * 字段
 *
 * @author  ZJ
 * */
public class Column {

    private String name;

    private String remark;

    private DataType dataType;

    private int length;

    private boolean isPrimaryKey;

    public Column(String name, String remark, DataType dataType, int length, boolean isPrimaryKey) {
        this.name = name;
        this.remark = remark;
        this.dataType = dataType;
        this.length = length;
        this.isPrimaryKey = isPrimaryKey;
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

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
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

}