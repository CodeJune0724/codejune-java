package com.codejune.jdbc.query;

import com.codejune.common.Builder;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.jdbc.query.filter.Config;
import com.codejune.jdbc.query.filter.Expression;
import com.codejune.jdbc.query.filter.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 过滤
 *
 * @author ZJ
 * */
public final class Filter implements Builder {

    private final List<Expression> expressionList = new ArrayList<>();

    private Config config = new Config();

    public List<Expression> getExpressionList() {
        boolean cleanNull = config.getCleanNull() != null && config.getCleanNull();
        List<String> cleanNullExclude = config.getCleanNullExclude();
        return compareHandlerAction(this.expressionList, compare -> {
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

    public Config getConfig() {
        return config;
    }

    public Filter setConfig(Config config) {
        if (config == null) {
            return this;
        }
        this.config = config;
        return this;
    }

    /**
     * and
     *
     * @param group 一组表达式
     *
     * @return this
     * */
    public Filter and(Group group) {
        this.expressionList.add(new Expression(Expression.Connector.AND, group));
        return this;
    }

    /**
     * and
     *
     * @param compare 比较
     *
     * @return this
     * */
    public Filter and(Compare compare) {
        this.expressionList.add(new Expression(Expression.Connector.AND, compare));
        return this;
    }

    /**
     * or
     *
     * @param group 一组表达式
     *
     * @return this
     * */
    public Filter or(Group group) {
        this.expressionList.add(new Expression(Expression.Connector.OR, group));
        return this;
    }

    /**
     * or
     *
     * @param compare 比较
     *
     * @return this
     * */
    public Filter or(Compare compare) {
        this.expressionList.add(new Expression(Expression.Connector.OR, compare));
        return this;
    }

    /**
     * Compare处理
     *
     * @param action action
     *
     * @return this
     * */
    public Filter compareHandler(Function<Compare, Compare> action) {
        List<Expression> newExpressionList = compareHandlerAction(this.expressionList, action);
        this.expressionList.clear();
        this.expressionList.addAll(newExpressionList);
        return this;
    }

    /**
     * 添加表达式
     *
     * @param expressionList expressionList
     * */
    public Filter addExpression(List<Expression> expressionList) {
        this.expressionList.addAll(expressionList);
        return this;
    }

    @Override
    public void build(Object object) {
        Map<String, Object> map = MapUtil.parse(object, String.class, Object.class);
        if (map == null) {
            return;
        }
        this.expressionList.clear();
        ObjectUtil.assignment(this.getConfig(), map.remove("$config"));
        Function<Map<?, ?>, List<Expression>> expressionAction = new Function<>() {
            @Override
            public List<Expression> apply(Map<?, ?> map) {
                if (map == null) {
                    return new ArrayList<>();
                }
                List<Object> or = ArrayUtil.parse(MapUtil.get(map, "$or", List.class), Object.class);
                if (or == null) {
                    or = new ArrayList<>();
                }
                List<Object> and = ArrayUtil.parse(MapUtil.get(map, "$and", List.class), Object.class);
                if (and == null) {
                    and = new ArrayList<>();
                }
                for (Object key : map.keySet()) {
                    if ("$or".equals(key) || "$and".equals(key)) {
                        continue;
                    }
                    Object value = map.get(key);
                    if (value instanceof Map<?, ?> valueMap) {
                        for (Object valueMapKey : valueMap.keySet()) {
                            and.add(new HashMap<>() {
                                {
                                    put(key, new HashMap<>() {
                                        {
                                            put(valueMapKey, valueMap.get(valueMapKey));
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        and.add(new HashMap<>() {
                            {
                                put(ObjectUtil.toString(key), value);
                            }
                        });
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
                        compare = or.get(0);
                    } else {
                        connector = Expression.Connector.AND;
                        compare = and.get(0);
                    }
                    result.add(new Expression(connector, ObjectUtil.transform(compare, Compare.class)));
                } else {
                    for (Object item : or) {
                        List<Expression> orExpressionList = this.apply(MapUtil.parse(item));
                        if (orExpressionList.isEmpty()) {
                            continue;
                        }
                        if (orExpressionList.size() == 1) {
                            result.add(new Expression(Expression.Connector.OR, orExpressionList.get(0).getCompare()));
                        } else {
                            Group group = new Group();
                            group.setExpressionList(orExpressionList);
                            result.add(new Expression(Expression.Connector.OR, group));
                        }
                    }
                    for (Object item : and) {
                        List<Expression> andExpressionList = this.apply(MapUtil.parse(item));
                        if (andExpressionList.isEmpty()) {
                            continue;
                        }
                        if (andExpressionList.size() == 1) {
                            result.add(new Expression(Expression.Connector.AND, andExpressionList.get(0).getCompare()));
                        } else {
                            Group group = new Group();
                            group.setExpressionList(andExpressionList);
                            result.add(new Expression(Expression.Connector.AND, group));
                        }
                    }
                }
                return result;
            }
        };
        this.expressionList.addAll(expressionAction.apply(map));
    }

    private static List<Expression> compareHandlerAction(List<Expression> expressionList, Function<Compare, Compare> action) {
        Function<List<Expression>, List<Expression>> function = new Function<>() {
            @Override
            public List<Expression> apply(List<Expression> expressionsList) {
                List<Expression> result = new ArrayList<>();
                for (Expression expression : expressionsList) {
                    if (expression.isCompare()) {
                        Compare compare = action.apply(expression.getCompare());
                        if (compare != null) {
                            result.add(expression);
                        }
                    }
                    if (expression.isGroup()) {
                        Group group = expression.getGroup();
                        List<Expression> newGroupExpressionList = this.apply(group.getExpressionList());
                        if (!ObjectUtil.isEmpty(newGroupExpressionList)) {
                            group.getExpressionList().clear();
                            group.getExpressionList().addAll(newGroupExpressionList);
                            result.add(expression);
                        }
                    }
                }
                return result;
            }
        };
        return function.apply(expressionList);
    }

}