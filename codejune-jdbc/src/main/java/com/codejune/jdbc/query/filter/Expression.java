package com.codejune.jdbc.query.filter;

import com.codejune.core.BaseException;

/**
 * 表达式
 *
 * @author ZJ
 * */
public final class Expression {

    private Connector connector;

    private Compare compare;

    private Group group;

    public Expression(Connector connector, Group group) {
        this(connector, group, null);
    }

    public Expression(Connector connector, Compare compare) {
        this(connector, null, compare);
    }

    private Expression(Connector connector, Group group, Compare compare) {
        if (connector == null) {
            throw new BaseException("connector is null");
        }
        if ((group == null && compare == null) || (group != null && compare != null)) {
            throw new BaseException("group && compare error");
        }
        this.connector = connector;
        this.group = group;
        this.compare = compare;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public Compare getCompare() {
        return compare;
    }

    public void setCompare(Compare compare) {
        this.compare = compare;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * isCompare
     *
     * @return isCompare
     * */
    public boolean isCompare() {
        return this.getCompare() != null;
    }

    /**
     * isGroup
     *
     * @return isGroup
     * */
    public boolean isGroup() {
        return this.getGroup() != null;
    }

    public enum Connector {

        AND,

        OR

    }

}