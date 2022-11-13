package com.codejune.jdbc.query;

/**
 * 字段
 *
 * @author ZJ
 * */
public final class Field {

    private String name;

    private String alias;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

}