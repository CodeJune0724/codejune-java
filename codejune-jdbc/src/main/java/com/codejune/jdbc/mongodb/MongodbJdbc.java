package com.codejune.jdbc.mongodb;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.StringUtil;
import com.mongodb.*;
import com.codejune.Jdbc;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.ArrayList;
import java.util.List;

/**
 * MongodbJdbc
 *
 * @author ZJ
 * */
public class MongodbJdbc implements Jdbc {

    final MongoClient mongoClient;

    private String defaultDatabase = null;

    public MongodbJdbc(String host, int port, String database, String username, String password) {
        MongoCredential mongoCredential = MongoCredential.createScramSha256Credential(username, database, password.toCharArray());
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyToClusterSettings(builder -> builder.hosts(ArrayUtil.asList(new ServerAddress(host, port)))).credential(mongoCredential).build();
        this.mongoClient = MongoClients.create(mongoClientSettings);
    }

    public MongodbJdbc(String host, int port) {
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyToClusterSettings(builder -> builder.hosts(ArrayUtil.asList(new ServerAddress(host, port)))).build();
        this.mongoClient = MongoClients.create(mongoClientSettings);
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
            throw new BaseException("databaseName is null");
        }
        return new MongodbDatabase(this, databaseName);
    }

    @Override
    public final List<MongodbDatabase> getDatabase() {
        List<MongodbDatabase> result = new ArrayList<>();
        for (String item : mongoClient.listDatabaseNames()) {
            result.add(getDatabase(item));
        }
        return result;
    }

    @Override
    public final MongodbDatabase switchDatabase(String databaseName) {
        return getDatabase(databaseName);
    }

    @Override
    public final MongodbDatabase getDefaultDatabase() {
        if (StringUtil.isEmpty(this.defaultDatabase)) {
            List<MongodbDatabase> databases = this.getDatabase();
            if (databases.isEmpty()) {
                throw new BaseException("mongoCredential is null");
            }
            this.defaultDatabase = databases.getFirst().getName();
        }
        return getDatabase(this.defaultDatabase);
    }

}