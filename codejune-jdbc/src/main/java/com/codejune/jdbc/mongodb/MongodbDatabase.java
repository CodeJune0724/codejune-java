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

    private final String databaseName;

    final MongoDatabase mongoDatabase;

    public MongodbDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        this.databaseName = mongoDatabase.getName();
    }

    public MongodbDatabase(MongodbJdbc mongodbJdbc, String databaseName) {
        this.databaseName = databaseName;
        this.mongoDatabase = mongodbJdbc.mongoClient.getDatabase(databaseName);
    }

    @Override
    public String getName() {
        return databaseName;
    }

    @Override
    public MongodbTable getTable(String tableName) {
        if (StringUtil.isEmpty(tableName)) {
            throw new BaseException("tableName is null");
        }
        return new MongodbTable(this, tableName);
    }

    @Override
    public List<MongodbTable> getTables() {
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