package com.codejune.jdbc.query;

import com.codejune.common.util.ObjectUtil;
import java.util.function.Function;

/**
 * 排序
 *
 * @author ZJ
 * */
public final class Sort {

    private String field;

    private OderBy orderBy = OderBy.ASC;

    public String getField() {
        return field;
    }

    public Sort setField(Object field) {
        if (field != null) {
            this.field = ObjectUtil.toString(field);
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
    public void keyHandler(Function<String, String> action) {
        if (action == null) {
            return;
        }
        this.setField(action.apply(this.field));
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