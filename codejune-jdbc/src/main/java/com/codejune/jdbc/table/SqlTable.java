package com.codejune.jdbc.table;

import com.codejune.jdbc.Column;
import com.codejune.jdbc.Table;
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
     * 获取表备注
     *
     * @return 表备注
     * */
    String getRemark();

    /**
     * 修改表名
     *
     * @param newTableName 新表名
     * */
    void rename(String newTableName);

}