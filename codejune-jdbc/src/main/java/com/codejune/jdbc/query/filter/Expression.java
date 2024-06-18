package com.codejune.jdbc.query.filter;

import com.codejune.core.BaseException;
import com.codejune.jdbc.query.Filter;

/**
 * 表达式
 *
 * @author ZJ
 * */
public final class Expression {

    private final Connector connector;

    private final Object expression;

    public Expression(Connector connector, Object expression) {
        if (!(expression instanceof Filter) && !(expression instanceof Compare)) {
            throw new BaseException("expression error");
        }
        this.connector = connector;
        this.expression = expression;
    }

    public Connector getConnector() {
        return connector;
    }

    /**
     * 是否是filter
     *
     * @return boolean
     * */
    public boolean isFilter() {
        return this.expression instanceof Filter;
    }

    /**
     * 是否是Compare
     *
     * @return boolean
     * */
    public boolean isCompare() {
        return this.expression instanceof Compare;
    }

    /**
     * 获取filter
     *
     * @return Filter
     * */
    public Filter getFilter() {
        if (this.isFilter()) {
            return (Filter) this.expression;
        }
        throw new BaseException("not filter");
    }

    /**
     * 获取Compare
     *
     * @return Compare
     * */
    public Compare getCompare() {
        if (this.isCompare()) {
            return (Compare) this.expression;
        }
        throw new BaseException("not compare");
    }

    public enum Connector {

        AND,

        OR

    }

}