package com.codejune.jdbc.query;

import com.codejune.common.Action;
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
     * @param action action
     * */
    public void keyHandler(Action<String, String> action) {
        if (action == null) {
            return;
        }
        this.column = action.then(this.column);
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