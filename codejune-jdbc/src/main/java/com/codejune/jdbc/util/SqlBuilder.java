package com.codejune.jdbc.util;

import com.codejune.Jdbc;
import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.ObjectHandler;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.access.AccessDatabaseJdbc;
import com.codejune.jdbc.mysql.MysqlJdbc;
import com.codejune.jdbc.query.Filter;
import java.util.*;

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
            throw new InfoException("tableName is null");
        }
        if (jdbcType == null) {
            throw new InfoException("jdbcType is null");
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
        String sql = "SELECT " + (query.getField().size() == 0 ? "*" : ArrayUtil.toString(query.getField(), field -> {
            String result = "";
            if (!StringUtil.isEmpty(field.getName())) {
                result = field.getName();
            }
            if (!StringUtil.isEmpty(field.getAlias())) {
                result = result + " " + field.getAlias();
            }
            return result;
        }, ", ")) + " FROM " + tableName;
        sql = sql + " " + parseWhere(query.getFilter());
        if (query.isSort()) {
            sql = StringUtil.append(sql, " ORDER BY ", ArrayUtil.toString(query.getSort(), sort -> sort.getColumn() + " " + sort.getOrderBy().name(), ", "));
        }
        if (query.isPage()) {
            Integer page = query.getPage();
            Integer size = query.getSize();
            if (jdbcType == MysqlJdbc.class) {
                sql = StringUtil.append("SELECT * FROM (", sql, ") T LIMIT ", ((page - 1) * size) + "", ", ", size + "");
            } else {
                sql = StringUtil.append("SELECT ROWNUM R, T.* FROM (", sql, ") T");
                sql = StringUtil.append("SELECT * FROM (SELECT * FROM (", sql, ") WHERE R <= ", (page * size) + "", ") WHERE R >= ", (size * (page - 1) + 1) + "");
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
        String result = "WHERE 1 = 1";
        if (filter == null) {
            return result;
        }
        List<Filter> orList = filter.getOr();
        for (Filter or : orList) {
            String formatFilter = parseWhere(or);
            if (!StringUtil.isEmpty(formatFilter)) {
                String s = "(" + formatFilter + ")";
                if  (!StringUtil.isEmpty(result)) {
                    s = StringUtil.append(" OR ", s);
                }
                result = StringUtil.append(result, s);
            }
        }
        List<Filter.Item> andList = filter.getAnd();
        for (Filter.Item and : andList) {
            String formatItem = filterItemHandler(and);
            if (!StringUtil.isEmpty(formatItem)) {
                String s = "(" + formatItem + ")";
                if  (!StringUtil.isEmpty(result)) {
                    s = StringUtil.append(" AND ", s);
                }
                result = StringUtil.append(result, s);
            }
        }
        return result;
    }

    private String filterItemHandler(Filter.Item item) {
        String result = "";
        if (item == null) {
            return result;
        }
        Filter.Item.Type type = item.getType();
        String key = item.getKey();
        Object value = item.getValue();
        switch (type) {
            case GT:
                result = key + " > " + valueHandler(value);
                break;
            case GTE:
                result = key + " >= " + valueHandler(value);
                break;
            case LT:
                result = key + " < " + valueHandler(value);
                break;
            case LTE:
                result = key + " <= " + valueHandler(value);
                break;
            case EQUALS:
                if (value == null) {
                    result = key + " IS " + valueHandler(null);
                } else {
                    if (jdbcType == AccessDatabaseJdbc.class && value instanceof String) {
                        result = "StrComp(" + key + ", " + valueHandler(value) + ", 0) = 0";
                    } else {
                        result = key + " = " + valueHandler(value);
                    }
                }
                break;
            case NOT_EQUALS:
                if (value == null) {
                    result = key + " IS NOT " + valueHandler(null);
                } else {
                    result = key + " != " + valueHandler(value);
                }
                break;
            case CONTAINS:
                result = key + " LIKE '%" + ObjectUtil.toString(value) + "%'";
                break;
            case NOT_CONTAINS:
                result = key + " NOT LIKE '%" + ObjectUtil.toString(value) + "%'";
                break;
            case IN:
                String in = inHandler(key, value, false);
                if (in != null) {
                    result = in;
                }
                break;
            case NOT_IN:
                String notIn = inHandler(key, value, true);
                if (notIn != null) {
                    result = notIn;
                }
                break;
        }
        return result;
    }

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
        if (value instanceof ObjectHandler) {
            return ObjectUtil.toString(((ObjectHandler) value).getNewObject(value));
        }
        String result = ObjectUtil.toString(value);
        if (result == null) {
            return valueHandler(null);
        }
        return "'" + result.replaceAll("'", "''") + "'";
    }

    private String inHandler(String key, Object value, boolean isNot) {
        if (StringUtil.isEmpty(key)) {
            throw new InfoException("不准为null");
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