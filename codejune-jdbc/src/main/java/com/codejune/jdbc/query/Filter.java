package com.codejune.jdbc.query;

import com.codejune.core.Builder;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.MapUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.jdbc.query.filter.Config;
import com.codejune.jdbc.query.filter.Expression;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 过滤
 *
 * @author ZJ
 * */
public final class Filter implements Builder {

    private Config config = null;

    private List<Expression> expression = new ArrayList<>();

    public Config getConfig() {
        if (this.config == null) {
            this.config = new Config();
        }
        return this.config;
    }

    public Filter setConfig(Config config) {
        this.config = config;
        return this;
    }

    public List<Expression> getExpression() {
        boolean cleanNull = config.getCleanNull() != null && config.getCleanNull();
        List<String> cleanNullExclude = config.getCleanNullExclude();
        return expressionHandler(this.expression, compare -> {
            if (compare == null) {
                return null;
            }
            if (cleanNull) {
                if (ObjectUtil.isEmpty(compare.getValue()) && (cleanNullExclude == null || !cleanNullExclude.contains(compare.getKey()))) {
                    return null;
                }
            }
            return compare;
        });
    }

    /**
     * and
     *
     * @param compare compare
     *
     * @return this
     * */
    public Filter and(Compare compare) {
        if (compare == null) {
            return this;
        }
        this.expression.add(new Expression(Expression.Connector.AND, compare));
        return this;
    }

    /**
     * and
     *
     * @param consumer consumer
     *
     * @return this
     * */
    public Filter and(Consumer<Filter> consumer) {
        if (consumer == null) {
            return this;
        }
        Filter filter = new Filter();
        consumer.accept(filter);
        this.expression.add(new Expression(Expression.Connector.AND, filter));
        return this;
    }

    /**
     * or
     *
     * @param compare compare
     *
     * @return this
     * */
    public Filter or(Compare compare) {
        if (compare == null) {
            return this;
        }
        this.expression.add(new Expression(Expression.Connector.OR, compare));
        return this;
    }

    /**
     * or
     *
     * @param consumer consumer
     *
     * @return this
     * */
    public Filter or(Consumer<Filter> consumer) {
        if (consumer == null) {
            return this;
        }
        Filter filter = new Filter();
        consumer.accept(filter);
        this.expression.add(new Expression(Expression.Connector.OR, filter));
        return this;
    }

    /**
     * expressionHandler
     *
     * @param action action
     *
     * @return this
     * */
    public Filter expressionHandler(Function<Compare, Compare> action) {
        List<Expression> newExpressionList = expressionHandler(this.expression, action);
        this.expression.clear();
        this.expression.addAll(newExpressionList);
        return this;
    }

    @Override
    public void build(Object object) {
        Map<String, Object> map = MapUtil.parse(object, String.class, Object.class);
        if (map == null) {
            return;
        }
        this.setConfig(MapUtil.get(map, "$config", Config.class));
        map.remove("$config");
        Function<Map<?, ?>, List<Expression>> transformExpression = new Function<>() {
            @Override
            public List<Expression> apply(Map<?, ?> map) {
                if (map == null) {
                    return new ArrayList<>();
                }
                List<Map<String, Object>> or = ArrayUtil.parseListMap(MapUtil.get(map, "$or", List.class), String.class, Object.class);
                map.remove("$or");
                if (or == null) {
                    or = new ArrayList<>();
                }
                List<Map<String, Object>> and = ArrayUtil.parseListMap(MapUtil.get(map, "$and", List.class), String.class, Object.class);
                map.remove("$and");
                if (and == null) {
                    and = new ArrayList<>();
                }
                for (Object key : map.keySet()) {
                    Object value = map.get(key);
                    if (value instanceof Map<?, ?> valueOfMap) {
                        for (Object valueMapKey : valueOfMap.keySet()) {
                            and.add(MapUtil.asMap(
                                    new AbstractMap.SimpleEntry<>(ObjectUtil.toString(key), MapUtil.asMap(
                                            new AbstractMap.SimpleEntry<>(valueMapKey, valueOfMap.get(valueMapKey))
                                    ))
                            ));
                        }
                    } else {
                        and.add(MapUtil.asMap(
                                new AbstractMap.SimpleEntry<>(ObjectUtil.toString(key), value)
                        ));
                    }
                }
                int count = or.size() + and.size();
                if (count == 0) {
                    return new ArrayList<>();
                }
                List<Expression> result = new ArrayList<>();
                if (count == 1) {
                    Expression.Connector connector;
                    Object compare;
                    if (or.size() == 1) {
                        connector = Expression.Connector.OR;
                        compare = or.getFirst();
                    } else {
                        connector = Expression.Connector.AND;
                        compare = and.getFirst();
                    }
                    result.add(new Expression(connector, ObjectUtil.transform(compare, Compare.class)));
                } else {
                    for (Object item : or) {
                        List<Expression> orExpressionList = this.apply(MapUtil.parse(item));
                        if (orExpressionList.isEmpty()) {
                            continue;
                        }
                        if (orExpressionList.size() == 1) {
                            result.add(new Expression(Expression.Connector.OR, orExpressionList.getFirst().getCompare()));
                        } else {
                            Filter filter = new Filter();
                            filter.expression = orExpressionList;
                            result.add(new Expression(Expression.Connector.OR, filter));
                        }
                    }
                    for (Object item : and) {
                        List<Expression> andExpressionList = this.apply(MapUtil.parse(item));
                        if (andExpressionList.isEmpty()) {
                            continue;
                        }
                        if (andExpressionList.size() == 1) {
                            result.add(new Expression(Expression.Connector.AND, andExpressionList.getFirst().getCompare()));
                        } else {
                            Filter filter = new Filter();
                            filter.expression = andExpressionList;
                            result.add(new Expression(Expression.Connector.AND, filter));
                        }
                    }
                }
                return result;
            }
        };
        this.expression.clear();
        this.expression.addAll(transformExpression.apply(map));
    }

    private static List<Expression> expressionHandler(List<Expression> expression, Function<Compare, Compare> action) {
        if (ObjectUtil.isEmpty(expression) || action == null) {
            return expression;
        }
        List<Expression> result = new ArrayList<>();
        for (Expression item : expression) {
            if (item.isCompare()) {
                Compare compare = action.apply(item.getCompare());
                if (compare == null) {
                    continue;
                }
                result.add(new Expression(item.getConnector(), compare));
            } else if (item.isFilter()) {
                Filter filter = item.getFilter();
                List<Expression> newExpression = expressionHandler(filter.expression, action);
                if (ObjectUtil.isEmpty(newExpression)) {
                    continue;
                }
                Filter newFilter = new Filter();
                newFilter.expression = newExpression;
                result.add(new Expression(item.getConnector(), newFilter));
            }
        }
        return result;
    }

}