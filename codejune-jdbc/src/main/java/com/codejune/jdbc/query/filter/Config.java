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

}