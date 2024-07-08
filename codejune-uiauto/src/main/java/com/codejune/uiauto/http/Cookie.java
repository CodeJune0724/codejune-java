package com.codejune.uiauto.http;

import java.util.Date;

public final class Cookie {

    private final String name;

    private final String data;

    private final String domain;

    private final String path;

    private final Date expiry;

    private final boolean isSecure;

    private final boolean isHttpOnly;

    private final String sameSite;

    public Cookie(String name, String data, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly, String sameSite) {
        this.name = name;
        this.data = data;
        this.domain = domain;
        this.path = path;
        this.expiry = expiry;
        this.isSecure = isSecure;
        this.isHttpOnly = isHttpOnly;
        this.sameSite = sameSite;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public Date getExpiry() {
        return expiry;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public boolean isHttpOnly() {
        return isHttpOnly;
    }

    public String getSameSite() {
        return sameSite;
    }

}