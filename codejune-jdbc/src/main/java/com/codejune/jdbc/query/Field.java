package com.codejune.jdbc.query;

import com.codejune.core.BaseException;
import java.util.function.Function;

/**
 * 字段
 *
 * @author ZJ
 * */
public final class Field implements Cloneable {

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

    @Override
    public Field clone() {
        try {
            Field result = (Field) super.clone();
            result.name = this.name;
            result.alias = this.alias;
            return result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}