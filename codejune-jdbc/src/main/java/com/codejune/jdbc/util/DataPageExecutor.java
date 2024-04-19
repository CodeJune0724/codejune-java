package com.codejune.jdbc.util;

import com.codejune.common.BaseException;
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
public abstract class DataPageExecutor extends com.codejune.common.DataPageExecutor<Map<String, Object>> {

    private final Table table;

    private final Filter filter;

    public DataPageExecutor(int size, Table table, Filter filter) {
        super(size);
        if (table == null) {
            throw new BaseException("table is null");
        }
        this.table = table;
        this.filter = filter;
    }

    public DataPageExecutor(int size, Table table) {
        this(size, table, null);
    }

    @Override
    public final List<Map<String, Object>> query(int page, int size) {
        return table.query(new Query().setPage(page).setSize(size).setFilter(filter)).getData();
    }

}