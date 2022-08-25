package com.codejune.jdbc.util;

import com.codejune.Jdbc;
import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.KeyHandler;
import com.codejune.jdbc.sqlJdbc.AccessDatabaseJdbc;
import com.codejune.jdbc.sqlJdbc.OracleJdbc;
import com.codejune.jdbc.Filter;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import java.util.*;

/**
 * SqlUtil
 *
 * @author ZJ
 * */
public final class SqlUtil {

    /**
     * 将对象转成insertSql
     *
     * @param tableName 表名
     * @param object map
     *
     * @return insertSql
     * */
    public static String parseInsertSql(String tableName, Object object) {
        if (object == null) {
            return null;
        }

        Map<?, ?> map;
        if (object instanceof Map<?, ?>) {
            map = (Map<?, ?>) object;
        } else {
            map = ObjectUtil.transform(object, Map.class);
        }

        String sql = "INSERT INTO " + tableName + "(";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            sql = StringUtil.append(sql, key.toString(), ", ");
        }
        sql = StringUtil.append(sql.substring(0, sql.length() - 2), ") VALUES(");

        Collection<?> values = map.values();
        for (Object v : values) {
            String value;
            if (v == null) {
                value = "NULL";
            } else {
                value = formatValue(v);
            }
            value = value + ", ";
            sql = StringUtil.append(sql, value);
        }
        sql = StringUtil.append(sql.substring(0, sql.length() - 2), ")");

        return sql;
    }

    /**
     * 转换成where
     *
     * @param filter 过滤条件
     * @param jdbcType 数据库类型
     *
     * @return where 条件
     * */
    public static String toWhere(Filter filter, Class<? extends Jdbc> jdbcType) {
        String formatFilter = formatFilter(filter, jdbcType);
        if (StringUtil.isEmpty(formatFilter)) {
            return "";
        } else {
            return "WHERE 1 = 1 AND " + formatFilter;
        }
    }

    /**
     * 转换成where
     *
     * @param filter 过滤条件
     *
     * @return where 条件
     * */
    public static String toWhere(Filter filter) {
        return toWhere(filter, OracleJdbc.class);
    }

    /**
     * 生成update语句
     *
     * @param tableName 表名
     * @param filter 过滤
     * @param setData 设置的数据
     * @param jdbcType 数据库类型
     *
     * @return update sql
     * */
    public static String parseUpdateSql(String tableName, Filter filter, Map<String, Object> setData, Class<? extends Jdbc> jdbcType) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        if (setData == null) {
            return null;
        }
        String sql = "UPDATE " + tableName + " SET ";
        Set<String> keySet = setData.keySet();
        for (String key : keySet) {
            String value;
            Object o = setData.get(key);
            if (o == null) {
                value = "NULL";
            } else {
                value = formatValue(o);
            }
            sql = StringUtil.append(sql, key, " = ", value, ", ");
        }
        sql = sql.substring(0, sql.length() - 2);
        if (filter != null) {
            sql = StringUtil.append(sql, " ", toWhere(filter, jdbcType));
        }
        return sql;
    }

    /**
     * 生成update语句
     *
     * @param tableName 表名
     * @param filter 过滤
     * @param setData 设置的数据
     *
     * @return update sql
     * */
    public static String parseUpdateSql(String tableName, Filter filter, Map<String, Object> setData) {
        if (StringUtil.isEmpty(tableName)) {
            return null;
        }
        if (setData == null) {
            return null;
        }
        String sql = "UPDATE " + tableName + " SET ";
        Set<String> keySet = setData.keySet();
        for (String key : keySet) {
            String value;
            Object o = setData.get(key);
            if (o == null) {
                value = "NULL";
            } else {
                value = formatValue(o);
            }
            sql = StringUtil.append(sql, key, " = ", value, ", ");
        }
        sql = sql.substring(0, sql.length() - 2);
        if (filter != null) {
            sql = StringUtil.append(sql, " ", toWhere(filter, OracleJdbc.class));
        }
        return sql;
    }

    private static String formatFilter(Filter filter, Class<? extends Jdbc> jdbcType) {
        String result = "";
        if (filter == null) {
            return result;
        }
        List<Filter> or = filter.getOr();
        List<Filter.Item> and = filter.getAnd();
        for (Filter filter1 : or) {
            String formatFilter = formatFilter(filter1, jdbcType);
            if (!StringUtil.isEmpty(formatFilter)) {
                String s = "(" + formatFilter + ")";
                if  (!StringUtil.isEmpty(result)) {
                    s = StringUtil.append(" OR ", s);
                }
                result = StringUtil.append(result, s);
            }
        }
        for (Filter.Item item : and) {
            String formatItem = formatItem(item, jdbcType);
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

    private static String formatItem(Filter.Item item, Class<? extends Jdbc> jdbcType) {
        String result = "";
        if (item == null) {
            return result;
        }
        Filter.Item.Type type = item.getType();
        String key = item.getKey();
        Object value = item.getValue();
        switch (type) {
            case GT:
                result = key + " > " + formatValue(value);
                break;
            case GTE:
                result = key + " >= " + formatValue(value);
                break;
            case LT:
                result = key + " < " + formatValue(value);
                break;
            case LTE:
                result = key + " <= " + formatValue(value);
                break;
            case EQUALS:
                if (value == null) {
                    result = key + " IS " + formatValue(value);
                } else {
                    if (jdbcType == AccessDatabaseJdbc.class) {
                        if (value instanceof Number) {
                            result = key + " = " + formatValue(value);
                        } else {
                            result = "StrComp(" + key + ", " + formatValue(value) + ", 0) = 0";
                        }
                    } else {
                        result = key + " = " + formatValue(value);
                    }
                }
                break;
            case NOT_EQUALS:
                if (value == null) {
                    result = key + " IS NOT " + formatValue(value);
                } else {
                    result = key + " != " + formatValue(value);
                }
                break;
            case CONTAINS:
                result = key + " LIKE '%" + ObjectUtil.toString(value) + "%'";
                break;
            case NOT_CONTAINS:
                result = key + " NOT LIKE '%" + ObjectUtil.toString(value) + "%'";
                break;
            case IN:
                String in = formatIn(key, value, false);
                if (in != null) {
                    result = in;
                }
                break;
            case NOT_IN:
                String notIn = formatIn(key, value, true);
                if (notIn != null) {
                    result = notIn;
                }
                break;
        }
        return result;
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Date) {
            return "TO_DATE('" + DateUtil.format((Date) value, DateUtil.DEFAULT_DATE_FORMAT) + "', 'yyyy-mm-dd hh24:mi:ss')";
        } else if (value instanceof KeyHandler) {
            KeyHandler keyHandler = (KeyHandler) value;
            return formatValue(keyHandler.getNewKey(value));
        } else {
            String s = ObjectUtil.toString(value);
            if (s == null) {
                return formatValue(s);
            }
            s = s.replaceAll("'", "''");
            return "'" + s + "'";
        }
    }

    private static String formatIn(String key, Object value, boolean isNot) {
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
            inSql = StringUtil.append(inSql, formatValue(o), ", ");
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