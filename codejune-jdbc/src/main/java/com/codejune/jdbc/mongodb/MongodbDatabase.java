package com.codejune.jdbc.mongodb;

import com.codejune.core.BaseException;
import com.codejune.core.util.StringUtil;
import com.codejune.jdbc.Database;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;

/**
 * MongodbDatabase
 *
 * @author ZJ
 * */
public final class MongodbDatabase implements Database {

    private final MongodbJdbc mongodbJdbc;

    private final String name;

    final MongoDatabase mongoDatabase;

    public MongodbDatabase(MongoDatabase mongoDatabase) {
        this.name = mongoDatabase.getName();
        this.mongodbJdbc = null;
        this.mongoDatabase = mongoDatabase;
    }

    public MongodbDatabase(MongodbJdbc mongodbJdbc, String name) {
        this.mongodbJdbc = mongodbJdbc;
        this.name = name;
        this.mongoDatabase = mongodbJdbc.mongoClient.getDatabase(name);
    }

    @Override
    public MongodbJdbc getJdbc() {
        return this.mongodbJdbc;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public MongodbTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new BaseException("tableName is null");
        }
        return new MongodbTable(this, tableName);
    }

    @Override
    public List<MongodbTable> getTable() {
        List<MongodbTable> result = new ArrayList<>();
        for (String collection: mongoDatabase.listCollectionNames()) {
            result.add(getTable(collection));
        }
        return result;
    }

    @Override
    public void deleteTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new BaseException("tableName is null");
        }
        mongoDatabase.getCollection(tableName).drop();
    }

}