package com.codejune.jdbc.query;

import java.util.function.Function;

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

    /**
     * 设置key
     *
     * @param action action
     * */
    public void keyHandler(Function<String, String> action) {
        if (action == null) {
            return;
        }
        this.setName(action.apply(this.name));
    }

}