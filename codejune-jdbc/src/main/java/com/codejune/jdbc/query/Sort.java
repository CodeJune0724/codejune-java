package com.codejune.jdbc.query;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import java.util.function.Function;

/**
 * 排序
 *
 * @author ZJ
 * */
public final class Sort implements Cloneable {

    private String field;

    private OderBy orderBy = OderBy.ASC;

    public Sort(Object field) {
        this.setField(field);
    }

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

    @Override
    public Sort clone() {
        try {
            Sort result = (Sort) super.clone();
            result.field = this.field;
            result.orderBy = this.orderBy;
            return result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
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