package com.codejune.jdbc.handler;

import com.codejune.jdbc.Filter;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.Table;
import java.util.Map;

/**
 * 数据分页处理
 *
 * @author ZJ
 * */
public abstract class DataPageHandler extends com.codejune.common.handler.DataPageHandler<Map<String, Object>> {

    public DataPageHandler(int size, Table table, Filter filter) {
        super(size, (page, size1) -> table.query(new Query().setPage(page).setSize(size1).setFilter(filter)).getData());
    }

    public DataPageHandler(int size, Table table) {
        this(size, table, null);
    }

}