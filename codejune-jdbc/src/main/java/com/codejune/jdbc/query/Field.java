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

    public Field setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Field setAlias(String alias) {
        this.alias = alias;
        return this;
    }

}