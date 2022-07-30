package com.codejune.jdbc.table;

import com.codejune.jdbc.Table;
import com.codejune.common.model.Column;
import com.codejune.common.model.Filter;
import com.codejune.common.util.StringUtil;
import java.util.List;

public interface SqlTable extends Table {

    /**
     * 获取所有字段
     *
     * @return 所有字段
     * */
    List<Column> getColumns();

    /**
     * 获取字段
     *
     * @param columnName 字段名
     *
     * @return 类型
     * */
    default Column getColumn(String columnName) {
        List<Column> columns = this.getColumns();
        if (columns == null || StringUtil.isEmpty(columnName)) {
            return null;
        }
        for (Column column : columns) {
            if (columnName.equals(column.getName())) {
                return column;
            }
        }
        return null;
    }

    /**
     * 过滤filter中不存在的字段
     *
     * @param filter filter
     * */
    default void columnFilter(Filter filter) {
        if (filter == null) {
            return;
        }
        filter.filterKey(getColumns());
    }

}