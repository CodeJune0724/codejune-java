package com.codejune.jdbc.util;

import com.codejune.Jdbc;
import com.codejune.common.BaseException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.jdbc.mysql.MysqlJdbc;
import com.codejune.jdbc.query.Filter;
import com.codejune.jdbc.query.filter.Compare;
import com.codejune.jdbc.query.filter.Expression;
import com.codejune.jdbc.query.filter.Group;
import java.util.*;
import java.util.function.Function;

/**
 * Sql生成器
 *
 * @author ZJ
 * */
public final class SqlBuilder {

    private final String tableName;

    private final Class<? extends Jdbc> jdbcType;

    public SqlBuilder(String tableName, Class<? extends Jdbc> jdbcType) {
        if (StringUtil.isEmpty(tableName)) {
            throw new BaseException("tableName is null");
        }
        if (jdbcType == null) {
            throw new BaseException("jdbcType is null");
        }
        this.tableName = tableName;
        this.jdbcType = jdbcType;
    }

    /**
     * 生成insertSql
     *
     * @param object object
     *
     * @return insertSql
     * */
    public String parseInsertSql(Object object) {
        if (object == null) {
            return null;
        }
        Map<?, ?> map = ObjectUtil.transform(object, Map.class);
        String sql = "INSERT INTO " + tableName + "(";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            sql = StringUtil.append(sql, key.toString(), ", ");
        }
        sql = StringUtil.append(sql.substring(0, sql.length() - 2), ") VALUES (");
        Collection<?> values = map.values();
        for (Object v : values) {
            String value = valueHandler(v);
            value = value + ", ";
            sql = StringUtil.append(sql, value);
        }
        sql = StringUtil.append(sql.substring(0, sql.length() - 2), ")");
        return sql;
    }

    /**
     * 生成deleteSql
     *
     * @param filter filter
     *
     * @return deleteSql
     * */
    public String parseDeleteSql(Filter filter) {
        return "DELETE FROM " + tableName + " " + parseWhere(filter);
    }

    /**
     * 生成updateSql
     *
     * @param setData 设置的数据
     * @param filter 过滤
     *
     * @return update sql
     * */
    public String parseUpdateSql(Map<String, Object> setData, Filter filter) {
        if (ObjectUtil.isEmpty(setData)) {
            return null;
        }
        String sql = "UPDATE " + tableName + " SET ";
        Set<String> keySet = setData.keySet();
        for (String key : keySet) {
            String value = valueHandler(setData.get(key));
            sql = StringUtil.append(sql, key, " = ", value, ", ");
        }
        sql = sql.substring(0, sql.length() - 2);
        if (filter != null) {
            sql = StringUtil.append(sql, " ", parseWhere(filter));
        }
        return sql;
    }

    /**
     * 生成count查询语句
     *
     * @param filter filter
     *
     * @return count查询语句
     * */
    public String parseCountSql(Filter filter) {
        return "SELECT COUNT(*) C FROM " + tableName + " " + parseWhere(filter);
    }

    /**
     * 生成查询数据语句
     *
     * @param query query
     *
     * @return 查询数据语句
     * */
    public String parseQueryDataSql(Query query) {
        if (query == null) {
            query = new Query();
        }
        String sql = "SELECT " + (query.getField().isEmpty() ? "*" : ArrayUtil.toString(query.getField(), field -> {
            String result = "";
            if (!StringUtil.isEmpty(field.getName())) {
                result = field.getName();
            }
            if (!StringUtil.isEmpty(field.getAlias())) {
                result = result + " AS '" + field.getAlias() + "'";
            }
            return result;
        }, ", ")) + " FROM " + tableName;
        sql = sql + " " + parseWhere(query.getFilter());
        if (!ObjectUtil.isEmpty(query.getSort())) {
            sql = StringUtil.append(sql, " ORDER BY ", ArrayUtil.toString(query.getSort(), sort -> sort.getField() + " " + sort.getOrderBy().name(), ", "));
        }
        if (query.paging()) {
            Integer page = query.getPage();
            Integer size = query.getSize();
            if (jdbcType == MysqlJdbc.class) {
                sql = StringUtil.append("SELECT * FROM (", sql, ") T LIMIT ", String.valueOf((page - 1) * size), ", ", String.valueOf(size));
            } else {
                sql = StringUtil.append("SELECT ROWNUM R, T.* FROM (", sql, ") T");
                sql = StringUtil.append("SELECT * FROM (SELECT * FROM (", sql, ") WHERE R <= ", String.valueOf(page * size), ") WHERE R >= ", String.valueOf(size * (page - 1) + 1));
            }
        }
        return sql;
    }

    /**
     * 转换where
     *
     * @param filter filter
     *
     * @return where
     * */
    public String parseWhere(Filter filter) {
        if (filter == null) {
            return "WHERE 1 = 1";
        }
        Function<Compare, String> compareAction = compare -> {
            String result = "";
            if (compare == null) {
                return result;
            }
            Compare.Type type = compare.getType();
            String key = compare.getKey();
            Object value = compare.getValue();
            switch (type) {
                case GT -> result = key + " > " + (value);
                case GTE -> result = key + " >= " + valueHandler(value);
                case LT -> result = key + " < " + valueHandler(value);
                case LTE -> result = key + " <= " + valueHandler(value);
                case EQUALS -> {
                    if (value == null) {
                        result = key + " IS " + valueHandler(null);
                    } else {
                        if (jdbcType == AccessDatabaseJdbc.class && value instanceof String) {
                            result = "(" + key + " IS NOT NULL) AND StrComp(" + key + ", " + valueHandler(value) + ", 0) = 0";
                        } else {
                            result = key + " = " + valueHandler(value);
                        }
                    }
                }
                case NOT_EQUALS -> {
                    if (value == null) {
                        result = key + " IS NOT " + valueHandler(null);
                    } else {
                        result = key + " != " + valueHandler(value);
                    }
                }
                case CONTAINS -> result = key + " LIKE '%" + ObjectUtil.toString(value) + "%'";
                case NOT_CONTAINS -> result = key + " NOT LIKE '%" + ObjectUtil.toString(value) + "%'";
                case IN -> {
                    String in = inHandler(key, value, false);
                    if (in != null) {
                        result = in;
                    }
                }
                case NOT_IN -> {
                    String notIn = inHandler(key, value, true);
                    if (notIn != null) {
                        result = notIn;
                    }
                }
                case START_WITH -> result = key + " LIKE '" + ObjectUtil.toString(value) + "%'";
                case NOT_START_WITH -> result = key + " NOT LIKE '" + ObjectUtil.toString(value) + "%'";
                case END_WITH -> result = key + " LIKE '%" + ObjectUtil.toString(value) + "'";
                case NOT_END_WITH -> result = key + " NOT LIKE '%" + ObjectUtil.toString(value) + "'";
            }
            return result;
        };
        Function<List<Expression>, String> expressionListActon = new Function<>() {
            @Override
            public String apply(List<Expression> expressionList) {
                if (ObjectUtil.isEmpty(expressionList)) {
                    return null;
                }
                String result = "";
                for (int i = 0; i < expressionList.size(); i++) {
                    Expression expression = expressionList.get(i);
                    if (expression == null) {
                        continue;
                    }
                    Expression.Connector connector = expression.getConnector();
                    if (connector == null) {
                        connector = Expression.Connector.AND;
                    }
                    String endString = "";
                    if (expression.isCompare()) {
                        String compareActionResult = compareAction.apply(expression.getCompare());
                        if (!StringUtil.isEmpty(compareActionResult)) {
                            endString = "(" + compareActionResult + ")";
                        }
                    }
                    if (expression.isGroup()) {
                        Group group = expression.getGroup();
                        if (group != null) {
                            String expressionListResult = this.apply(group.getExpressionList());
                            if (!StringUtil.isEmpty(expressionListResult)) {
                                endString = "(" + expressionListResult + ")";
                            }
                        }
                    }
                    if (!StringUtil.isEmpty(endString)) {
                        if (!StringUtil.isEmpty(result)) {
                            result = StringUtil.append(result, " ");
                        }
                        result = StringUtil.append(result, (i == 0 || StringUtil.isEmpty(result) ? endString : connector + " " + endString));
                    }
                }
                return result;
            }
        };
        String expressionListResult = expressionListActon.apply(filter.getExpressionList());
        if (StringUtil.isEmpty(expressionListResult)) {
            return "WHERE 1 = 1";
        } else {
            return "WHERE 1 = 1 AND " + expressionListResult;
        }
    }

    @SuppressWarnings("unchecked")
    private String valueHandler(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Date) {
            if (jdbcType == MysqlJdbc.class) {
                return "'" + DateUtil.format((Date) value, DateUtil.DEFAULT_DATE_FORMAT) + "'";
            }
            return "TO_DATE('" + DateUtil.format((Date) value, DateUtil.DEFAULT_DATE_FORMAT) + "', 'yyyy-mm-dd hh24:mi:ss')";
        }
        if (value instanceof Number) {
            return ObjectUtil.toString(value);
        }
        if (value instanceof Function<?,?>) {
            return ObjectUtil.toString(((Function<Object, Object>) value).apply(value));
        }
        String result = ObjectUtil.toString(value);
        if (result == null) {
            return valueHandler(null);
        }
        return "'" + result.replaceAll("'", "''") + "'";
    }

    private String inHandler(String key, Object value, boolean isNot) {
        if (StringUtil.isEmpty(key)) {
            throw new BaseException("不准为null");
        }
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        String inSql = "(";
        Set<Object> set = new HashSet<>();
        if (value instanceof List) {
            set.addAll((List<?>) value);
        } else {
            set.add(value);
        }
        boolean isContainsNull = false;
        for (Object o : set) {
            if (o == null) {
                isContainsNull = true;
                continue;
            }
            inSql = StringUtil.append(inSql, valueHandler(o), ", ");
        }
        inSql = inSql.substring(0, inSql.length() - 2) + ")";
        if (!isNot) {
            inSql = key + " IN " + inSql;
        } else {
            inSql = key + " NOT IN " + inSql;
        }
        if (isContainsNull) {
            if (!isNot) {
                return key + " IS NULL OR " + inSql;
            } else {
                return key + " IS NOT NULL AND " + inSql;
            }
        }  else {
            return inSql;
        }
    }

}