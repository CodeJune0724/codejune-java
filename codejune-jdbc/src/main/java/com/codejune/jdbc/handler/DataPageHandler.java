package com.codejune.jdbc.handler;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.Table;
import com.codejune.jdbc.query.Filter;
import java.util.List;
import java.util.Map;

/**
 * 数据分页处理
 *
 * @author ZJ
 * */
public abstract class DataPageHandler extends com.codejune.common.DataPageExecutor<Map<String, Object>> {

    private final Table table;

    private final Filter filter;

    public DataPageHandler(int size, Table table, Filter filter) {
        super(size);
        if (table == null) {
            throw new InfoException("table is null");
        }
        this.table = table;
        this.filter = filter;
    }

    public DataPageHandler(int size, Table table) {
        this(size, table, null);
    }

    @Override
    public final List<Map<String, Object>> queryData(int page, int size) {
        return table.query(new Query().setPage(page).setSize(size).setFilter(filter)).getData();
    }

}