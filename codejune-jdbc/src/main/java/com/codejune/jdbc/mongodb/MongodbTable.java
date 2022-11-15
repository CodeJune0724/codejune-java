package com.codejune.jdbc.mongodb;

import com.codejune.common.exception.ErrorException;
import com.codejune.common.util.MapUtil;
import com.codejune.jdbc.*;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.Sort;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.codejune.common.exception.InfoException;
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

    private final String tableName;

    private final MongoCollection<Document> mongoCollection;

    public MongodbTable(MongoDatabase mongoDatabase, String tableName) {
        this.tableName = tableName;
        mongoCollection = mongoDatabase.getCollection(tableName);
    }

    /**
     * 获取索引
     *
     * @return 所有的索引
     * */
    public List<Map<String, Object>> getIndexList() {
        ListIndexesIterable<Document> documents = mongoCollection.listIndexes();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Document document : documents) {
            result.add(new LinkedHashMap<>(document));
        }
        return result;
    }

    /**
     * 添加索引
     *
     * @param index index
     * */
    public void addIndex(Map<String, Object> index) {
        if (index == null) {
            return;
        }
        mongoCollection.createIndex(new Document(index));
    }

    /**
     * 删除索引
     *
     * @param index index
     * */
    public void deleteIndex(Map<String, Object> index) {
        if (index == null) {
            return;
        }
        mongoCollection.dropIndex(new Document(index));
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
            List<Document> documents = new ArrayList<>();
            for (Map<String, Object> map : data) {
                if (map.isEmpty()) {
                    continue;
                }
                documents.add(new Document(map));
            }
            mongoCollection.insertMany(documents);
            return data.size();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public long delete(Filter filter) {
        try {
            DeleteResult deleteResult = mongoCollection.deleteMany(formatFilter(filter));
            return deleteResult.getDeletedCount();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        try {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(true);
            BasicDBObject updateSetValue = new BasicDBObject("$set", setData);
            UpdateResult updateResult = mongoCollection.updateMany(formatFilter(filter), updateSetValue, updateOptions);
            return updateResult.getModifiedCount();
        } catch (Exception e) {
            throw new InfoException(e);
        }
    }

    @Override
    public long count(Filter filter) {
        return mongoCollection.countDocuments(formatFilter(filter));
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        if (query == null) {
            query = new Query();
        }
        FindIterable<Document> queryData = mongoCollection.find(formatFilter(query.getFilter()));

        if (query.isPage()) {
            queryData = queryData.limit(query.getSize()).skip(query.getSize() * (query.getPage() - 1));
        }

        if (query.isSort()) {
            List<Sort> sortList = query.getSort();
            for (Sort sort : sortList) {
                int sortInt;
                if (sort.getOrderBy() == Sort.OderBy.ASC) {
                    sortInt = 1;
                } else {
                    sortInt = -1;
                }
                queryData = queryData.sort(new Document().append(sort.getColumn(), sortInt));
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Document document : queryData) {
            Map<String, Object> map = new LinkedHashMap<>(document);
            result.add(map);
        }
        return result;
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

                if (result.get(key) != null) {
                    document.putAll(MapUtil.transformGeneric((Map<?, ?>) result.get(key), String.class, Object.class));
                }
                result.put(key, document);
            } else {
                throw new ErrorException("formatItem is error");
            }
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
                result.put("$eq", value);
                break;
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