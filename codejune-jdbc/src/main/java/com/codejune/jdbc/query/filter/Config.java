package com.codejune.jdbc.query.filter;

import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import java.util.List;

/**
 * config
 *
 * @author ZJ
 * */
public final class Config implements Cloneable {

    private Boolean cleanNull;

    private List<String> cleanNullExclude;

    public Boolean getCleanNull() {
        return cleanNull;
    }

    public Config setCleanNull(Boolean cleanNull) {
        this.cleanNull = cleanNull;
        return this;
    }

    public List<String> getCleanNullExclude() {
        return cleanNullExclude;
    }

    public Config setCleanNullExclude(List<String> cleanNullExclude) {
        this.cleanNullExclude = cleanNullExclude;
        return this;
    }

    @Override
    public Config clone() {
        try {
            Config result = (Config) super.clone();
            result.cleanNull = this.cleanNull;
            result.cleanNullExclude = ObjectUtil.clone(this.cleanNullExclude);
            return result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

}