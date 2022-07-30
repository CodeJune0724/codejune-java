package com.codejune.jdbc.table;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Table;
import com.codejune.common.model.Filter;
import com.codejune.common.model.Query;
import com.codejune.common.model.QueryResult;
import com.codejune.common.model.Sort;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.RegexUtil;
import org.bson.Document;
import java.util.*;
import java.util.regex.Pattern;

/**
 * MongodbTable
 *
 * @author ZJ
 * */
public final class MongodbTable implements Table {

    private final MongoDatabase mongoDatabase;

    private final String tableName;

    public MongodbTable(MongoDatabase mongoDatabase, String tableName) {
        this.mongoDatabase = mongoDatabase;
        this.tableName = tableName;
    }

    @Override
    public String getName() {
        return this.tableName;
    }

    @Override
    public long insert(List<Map<String, Object>> data) {
        try {
            if (ObjectUtil.isEmpty(data)) {
                return 0;
            }
            MongoCollection<Document> collection = getMongoCollection(getName());
            List<Document> documents = new ArrayList<>();
            for (Map<String, Object> map : data) {
                if (map.isEmpty()) {
                    continue;
                }
                documents.add(new Document(map));
            }
            collection.insertMany(documents);
            return data.size();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public long delete(Filter filter) {
        try {
            MongoCollection<Document> collection = getMongoCollection(getName());
            DeleteResult deleteResult = collection.deleteMany(formatFilter(filter));
            return deleteResult.getDeletedCount();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public long update(Filter filter, Map<String, Object> setData) {
        try {
            MongoCollection<Document> collection = getMongoCollection(getName());
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(true);
            BasicDBObject updateSetValue = new BasicDBObject("$set", setData);
            UpdateResult updateResult = collection.updateMany(formatFilter(filter), updateSetValue, updateOptions);
            return updateResult.getModifiedCount();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public QueryResult<Map<String, Object>> query(Query query) {
        if (query == null) {
            query = new Query();
        }

        MongoCollection<Document> collection = getMongoCollection(getName());
        Document filterDocument = formatFilter(query.getFilter());

        QueryResult<Map<String, Object>> result = new QueryResult<>();
        result.setCount(collection.countDocuments(filterDocument));

        FindIterable<Document> queryData = collection.find();

        // 过滤
        queryData = queryData.filter(filterDocument);

        // 分页
        if (query.isPage()) {
            queryData = queryData.limit(query.getSize()).skip(query.getSize() * (query.getPage() - 1));
        }

        // 排序
        if (query.isSort()) {
            Sort sort = query.getSort();
            int sortInt;
            if (sort.getOrderBy() == Sort.OderBy.ASC) {
                sortInt = 1;
            } else {
                sortInt = -1;
            }
            queryData = queryData.sort(new Document().append(sort.getColumn(), sortInt));
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Document document : queryData) {
            Map<String, Object> map = new LinkedHashMap<>(document);
            data.add(map);
        }
        result.setData(data);
        return result;
    }

    private MongoCollection<Document> getMongoCollection(String tableName) {
        return mongoDatabase.getCollection(tableName);
    }

    private static Document formatFilter(Filter filter) {
        Document result = new Document();
        if (filter == null) {
            filter = new Filter();
        }

        List<Filter> or = filter.getOr();
        List<Document> documentList = new ArrayList<>();
        for (Filter orFilter : or) {
            Document document = formatFilter(orFilter);
            if (document.size() != 0) {
                documentList.add(document);
            }
        }
        if (!ObjectUtil.isEmpty(documentList)) {
            result.put("$or", documentList);
        }

        List<Filter.Item> and = filter.getAnd();
        for (Filter.Item item : and) {
            String key = item.getKey();
            Object formatItem = formatItem(item);
            Object newValue;
            if (formatItem instanceof Map) {
                Document document;
                if (formatItem instanceof Document) {
                    document = (Document) formatItem;
                } else {
                    document = new Document();
                }
                Map<?, ?> formatItemMap = (Map<?, ?>) formatItem;
                Set<?> objects = formatItemMap.keySet();
                for (Object k : objects) {
                    document.put(k.toString(), formatItemMap.get(k));
                }
                newValue = document;
            } else {
                newValue = formatItem;
            }
            result.put(key, newValue);
        }

        return result;
    }

    private static Object formatItem(Filter.Item item) {
        if (item == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        Filter.Item.Type type = item.getType();
        Object value = item.getValue();
        switch (type) {
            case GT:
                result.put("$gt", value);
                break;
            case GTE:
                result.put("$gte", value);
                break;
            case LT:
                result.put("$lt", value);
                break;
            case LTE:
                result.put("$lte", value);
                break;
            case EQUALS:
                return value;
            case NOT_EQUALS:
                result.put("$ne", value);
                break;
            case IN:
                result.put("$in", value);
                break;
            case NOT_IN:
                result.put("$nin", value);
                break;
            case CONTAINS:
                result.put("$regex", Pattern.compile(RegexUtil.escape(value.toString())));
                break;
            case NOT_CONTAINS:
                Map<String, Object> map = new HashMap<>();
                map.put("$regex", Pattern.compile(RegexUtil.escape(value.toString())));
                result.put("$not", map);
                break;
        }
        return result;
    }

}