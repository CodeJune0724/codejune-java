package com.codejune.common.model;

import com.codejune.common.handler.KeyHandler;
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
     * @param keyHandler keyHandler
     * */
    public void setKey(KeyHandler keyHandler) {
        if (keyHandler == null) {
            return;
        }
        this.column = ObjectUtil.toString(keyHandler.getNewKey(this.column));
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