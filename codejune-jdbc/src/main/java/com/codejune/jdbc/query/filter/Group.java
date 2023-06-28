package com.codejune.jdbc.query.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * 一组表达式
 *
 * @author ZJ
 * */
public final class Group {

    private List<Expression> expressionList = new ArrayList<>();

    public Group() {}

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    /**
     * and
     *
     * @param group 组
     *
     * @return this
     * */
    public Group and(Group group) {
        this.expressionList.add(new Expression(Expression.Connector.AND, group));
        return this;
    }

    /**
     * and
     *
     * @param compare 比较
     *
     * @return this
     * */
    public Group and(Compare compare) {
        this.expressionList.add(new Expression(Expression.Connector.AND, compare));
        return this;
    }

    /**
     * or
     *
     * @param group 组
     *
     * @return this
     * */
    public Group or(Group group) {
        this.expressionList.add(new Expression(Expression.Connector.OR, group));
        return this;
    }

    /**
     * or
     *
     * @param compare 比较
     *
     * @return this
     * */
    public Group or(Compare compare) {
        this.expressionList.add(new Expression(Expression.Connector.OR, compare));
        return this;
    }

    /**
     * build
     *
     * @param group 组
     *
     * @return Group
     * */
    public static Group build(Group group) {
        return new Group().and(group);
    }

    /**
     * build
     *
     * @param compare 比较
     *
     * @return Group
     * */
    public static Group build(Compare compare) {
        return new Group().and(compare);
    }

    /**
     * build
     *
     * @return Group
     * */
    public static Group build() {
        return new Group();
    }

}