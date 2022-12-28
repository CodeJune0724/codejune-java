package com.codejune.jdbc.query;

import com.codejune.common.handler.DataHandler;
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
     * @param dataHandler dataHandler
     * */
    public void keyHandler(DataHandler<String, String> dataHandler) {
        if (dataHandler == null) {
            return;
        }
        this.column = dataHandler.handler(this.column);
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