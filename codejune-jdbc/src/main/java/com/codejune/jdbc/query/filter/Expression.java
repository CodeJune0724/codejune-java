package com.codejune.jdbc.query.filter;

import com.codejune.core.BaseException;
import com.codejune.jdbc.query.Filter;

/**
 * 表达式
 *
 * @author ZJ
 * */
public final class Expression implements Cloneable {

    private Connector connector;

    private Object expression;

    public Expression(Connector connector, Filter filter) {
        this.connector = connector;
        this.expression = filter;
    }

    public Expression(Connector connector, Compare compare) {
        this.connector = connector;
        this.expression = compare;
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

    @Override
    public Expression clone() {
        try {
            Expression result = (Expression) super.clone();
            result.connector = this.connector;
            if (this.expression instanceof Filter filter) {
                this.expression = filter.clone();
            } else if (this.expression instanceof Compare compare) {
                this.expression = compare.clone();
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * Connector
     *
     * @author ZJ
     * */
    public enum Connector {

        AND,

        OR

    }

}