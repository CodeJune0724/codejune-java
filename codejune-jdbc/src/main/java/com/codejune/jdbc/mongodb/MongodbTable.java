package com.codejune.jdbc.mongodb;

import com.codejune.core.util.MapUtil;
import com.codejune.jdbc.*;
import com.codejune.jdbc.query.Field;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.Sort;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.jdbc.query.filter.Expression;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.codejune.core.BaseException;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.RegexUtil;
import org.bson.Document;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * MongodbTable
 *
 * @author ZJ
 * */
public final class MongodbTable implements Table {

    private final String tableName;

    private final MongoCollection<Document> mongoCollection;

    MongodbTable(MongodbDatabase mongodbDatabase, String tableName) {
        this.tableName = tableName;
        mongoCollection = mongodbDatabase.mongoDatabase.getCollection(tableName);
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
            throw new BaseException(e);
        }
    }

    @Override
    public long delete(Filter filter) {
        try {
            DeleteResult deleteResult = mongoCollection.deleteMany(filterHandler(filter));
            return deleteResult.getDeletedCount();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public long update(Map<String, Object> setData, Filter filter) {
        try {
            UpdateOptions updateOptions = new UpdateOptions();
            updateOptions.upsert(true);
            BasicDBObject updateSetValue = new BasicDBObject("$set", setData);
            UpdateResult updateResult = mongoCollection.updateMany(filterHandler(filter), updateSetValue, updateOptions);
            return updateResult.getModifiedCount();
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public long count(Filter filter) {
        return mongoCollection.countDocuments(filterHandler(filter));
    }

    @Override
    public List<Map<String, Object>> queryData(Query query) {
        if (query == null) {
            query = new Query();
        }
        FindIterable<Document> queryData = mongoCollection.find(filterHandler(query.getFilter()));
        Map<String, String> fieldMap = new HashMap<>();
        if (!ObjectUtil.isEmpty(query.getField())) {
            Document document = new Document();
            for (Field field : query.getField()) {
                String name = field.getName();
                document.put(name, 1);
                fieldMap.put(name, field.getAlias());
            }
            queryData = queryData.projection(document);
        }
        if (query.paging()) {
            queryData = queryData.limit(query.getSize()).skip(query.getSize() * (query.getPage() - 1));
        }
        if (!ObjectUtil.isEmpty(query.getSort())) {
            List<Sort> sortList = query.getSort();
            for (Sort sort : sortList) {
                int sortInt;
                if (sort.getOrderBy() == Sort.OderBy.ASC) {
                    sortInt = 1;
                } else {
                    sortInt = -1;
                }
                queryData = queryData.sort(new Document().append(sort.getField(), sortInt));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Document document : queryData) {
            Map<String, Object> map = new LinkedHashMap<>(document);
            if (!ObjectUtil.isEmpty(fieldMap)) {
                map = MapUtil.parse(MapUtil.keyHandler(map, key -> {
                    String alias = fieldMap.get(key);
                    if (ObjectUtil.isEmpty(alias)) {
                        return key;
                    } else {
                        return alias;
                    }
                }), String.class, Object.class);
            }
            result.add(map);
        }
        return result;
    }

    private static Document filterHandler(Filter filter) {
        if (filter == null) {
            return new Document();
        }
        Function<Compare, Map<String, Object>> compareAction = compare -> {
            if (compare == null) {
                return null;
            }
            Map<String, Object> result = new HashMap<>();
            Compare.Type type = compare.getType();
            Object value = compare.getValue();
            switch (type) {
                case GT -> result.put("$gt", value);
                case GTE -> result.put("$gte", value);
                case LT -> result.put("$lt", value);
                case LTE -> result.put("$lte", value);
                case EQUALS -> result.put("$eq", value);
                case NOT_EQUALS -> result.put("$ne", value);
                case IN -> result.put("$in", value);
                case NOT_IN -> result.put("$nin", value);
                case CONTAINS -> result.put("$regex", Pattern.compile(RegexUtil.escape(ObjectUtil.toString(value))));
                case NOT_CONTAINS -> {
                    Map<String, Object> notContainsMap = new HashMap<>();
                    notContainsMap.put("$regex", Pattern.compile(RegexUtil.escape(ObjectUtil.toString(value))));
                    result.put("$not", notContainsMap);
                }
                case START_WITH ->
                        result.put("$regex", Pattern.compile("^" + RegexUtil.escape(ObjectUtil.toString(value))));
                case NOT_START_WITH -> {
                    Map<String, Object> notStartWithMap = new HashMap<>();
                    notStartWithMap.put("$regex", Pattern.compile("^" + RegexUtil.escape(ObjectUtil.toString(value))));
                    result.put("$not", notStartWithMap);
                }
                case END_WITH -> result.put("$regex", Pattern.compile(RegexUtil.escape(ObjectUtil.toString(value)) + "$"));
                case NOT_END_WITH -> {
                    Map<String, Object> notEndWithMap = new HashMap<>();
                    notEndWithMap.put("$regex", Pattern.compile(RegexUtil.escape(ObjectUtil.toString(value)) + "$"));
                    result.put("$not", notEndWithMap);
                }
            }
            return result;
        };
        Function<List<Expression>, Document> expressionListAction = new Function<>() {
            @Override
            public Document apply(List<Expression> expressionList) {
                if (ObjectUtil.isEmpty(expressionList)) {
                    return new Document();
                }
                Document result = new Document();
                List<Object> and = new ArrayList<>();
                List<Object> or = new ArrayList<>();
                for (Expression expression : expressionList) {
                    Expression.Connector connector = expression.getConnector();
                    if (connector == null) {
                        connector = Expression.Connector.AND;
                    }
                    if (expression.isCompare()) {
                        Compare compare = expression.getCompare();
                        Map<String, Object> compareActionResult = compareAction.apply(compare);
                        if (ObjectUtil.isEmpty(compareActionResult)) {
                            continue;
                        }
                        compareActionResult = MapUtil.asMap(
                                new AbstractMap.SimpleEntry<>(compare.getKey(), compareActionResult)
                        );
                        if (connector == Expression.Connector.AND) {
                            and.add(compareActionResult);
                        }
                        if (connector == Expression.Connector.OR) {
                            or.add(compareActionResult);
                        }
                    } else if (expression.isFilter()) {
                        Filter filter = expression.getFilter();
                        if (connector == Expression.Connector.AND) {
                            and.add(this.apply(filter.getExpression()));
                        }
                        if (connector == Expression.Connector.OR) {
                            or.add(this.apply(filter.getExpression()));
                        }
                    }
                }
                if (!ObjectUtil.isEmpty(or)) {
                    result.put("$or", or);
                }
                if (!ObjectUtil.isEmpty(and)) {
                    result.put("$and", and);
                }
                return result;
            }
        };
        return expressionListAction.apply(filter.getExpression());
    }

}