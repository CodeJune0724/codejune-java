package com.codejune.redis;

/**
 * config
 *
 * @author ZJ
 * */
public final class Config {

    private String host;

    private int port;

    private String password;

    private int database;

    public String getHost() {
        return host;
    }

    public Config setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Config setPort(int port) {
        this.port = port;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Config setPassword(String password) {
        this.password = password;
        return this;
    }

    public int getDatabase() {
        return database;
    }

    public Config setDatabase(int database) {
        this.database = database;
        return this;
    }

}