package com.codejune.jdbc.mongodb;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.codejune.Jdbc;
import com.codejune.common.util.StringUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * MongodbJdbc
 *
 * @author ZJ
 * */
public class MongodbJdbc implements Jdbc {

    private final MongoClient mongoClient;

    public MongodbJdbc(String host, int port, String database, String username, String password) {
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
        this.mongoClient = new MongoClient(new ServerAddress(host, port), credential, mongoClientOptions);
    }

    public MongodbJdbc(String host, int port) {
        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        this.mongoClient = new MongoClient(new ServerAddress(host, port), mongoClientOptions);
    }

    /**
     * 获取表
     *
     * @param database 数据库名
     * @param tableName 表名
     *
     * @return Table
     * */
    public MongodbTable getTable(String database, String tableName) {
        return new MongodbTable(this.mongoClient.getDatabase(database), tableName);
    }

    @Override
    public MongodbTable getTable(String tableName) {
        if (tableName.contains(".")) {
            String[] split = tableName.split("\\.");
            return getTable(split[0], split[1]);
        }
        return null;
    }

    @Override
    public List<MongodbTable> getTables(String database) {
        List<MongodbTable> result = new ArrayList<>();
        List<MongoDatabase> mongoDatabaseList = new ArrayList<>();
        if (StringUtil.isEmpty(database)) {
            MongoIterable<String> listDatabaseNames = this.mongoClient.listDatabaseNames();
            for (String d : listDatabaseNames) {
                result.addAll(getTables(d));
            }
        } else {
            mongoDatabaseList.add(this.mongoClient.getDatabase(database));
        }
        for (MongoDatabase mongoDatabase : mongoDatabaseList) {
            MongoIterable<String> collections = mongoDatabase.listCollectionNames();
            for (String collection: collections) {
                result.add(getTable(collection));
            }
        }
        return result;
    }

    @Override
    public List<MongodbTable> getTables() {
        return getTables(null);
    }

    @Override
    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

}