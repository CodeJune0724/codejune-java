package com.codejune.jdbc.query.filter;

import java.util.List;

/**
 * config
 *
 * @author ZJ
 * */
public final class Config {

    private Boolean cleanNull;

    private List<String> cleanNullExclude;

    public Boolean getCleanNull() {
        return cleanNull;
    }

    public void setCleanNull(Boolean cleanNull) {
        this.cleanNull = cleanNull;
    }

    public List<String> getCleanNullExclude() {
        return cleanNullExclude;
    }

    public void setCleanNullExclude(List<String> cleanNullExclude) {
        this.cleanNullExclude = cleanNullExclude;
    }

}