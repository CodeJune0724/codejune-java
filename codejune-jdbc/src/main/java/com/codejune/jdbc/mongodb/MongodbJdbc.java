package com.codejune.jdbc.mongodb;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Database;
import com.mongodb.*;
import com.mongodb.MongoClient;
import com.codejune.Jdbc;
import java.util.ArrayList;
import java.util.List;

/**
 * MongodbJdbc
 *
 * @author ZJ
 * */
public class MongodbJdbc implements Jdbc {

    final MongoClient mongoClient;

    public MongodbJdbc(String host, int port, String database, String username, String password) {
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
        this.mongoClient = new MongoClient(new ServerAddress(host, port), credential, mongoClientOptions);
    }

    public MongodbJdbc(String host, int port) {
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        this.mongoClient = new MongoClient(new ServerAddress(host, port), mongoClientOptions);
    }

    @Override
    public final void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Override
    public final MongodbDatabase getDatabase(String databaseName) {
        if (StringUtil.isEmpty(databaseName)) {
            throw new InfoException("databaseName is null");
        }
        return new MongodbDatabase(this, databaseName);
    }

    @Override
    public final List<MongodbDatabase> getDatabases() {
        List<MongodbDatabase> result = new ArrayList<>();
        for (String item : mongoClient.listDatabaseNames()) {
            result.add(getDatabase(item));
        }
        return result;
    }

    @Override
    public final Database switchDatabase(String databaseName) {
        return getDatabase(databaseName);
    }

    @Override
    public final Database getDefaultDatabase() {
        MongoCredential mongoCredential = mongoClient.getCredential();
        if (mongoCredential == null) {
            throw new InfoException("mongoCredential is null");
        }
        return getDatabase(mongoCredential.getUserName());
    }

}