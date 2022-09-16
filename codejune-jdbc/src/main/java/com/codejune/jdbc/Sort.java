package com.codejune.jdbc;

import com.codejune.common.handler.ObjectHandler;
import com.codejune.common.util.ObjectUtil;

/**
 * 排序
 *
 * @author ZJ
 * */
public final class Sort {

    private String column;

    private OderBy orderBy = OderBy.ASC;

    public String getColumn() {
        return column;
    }

    public Sort setColumn(Object column) {
        if (column != null) {
            this.column = ObjectUtil.toString(column);
        }
        return this;
    }

    public OderBy getOrderBy() {
        return orderBy;
    }

    public Sort setOrderBy(OderBy orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * 设置key
     *
     * @param objectHandler objectHandler
     * */
    public void setKey(ObjectHandler objectHandler) {
        if (objectHandler == null) {
            return;
        }
        this.column = ObjectUtil.toString(objectHandler.getNewObject(this.column));
    }

    /**
     * 排序类型
     *
     * @author ZJ
     * */
    public enum OderBy {

        ASC,

        DESC

    }

}