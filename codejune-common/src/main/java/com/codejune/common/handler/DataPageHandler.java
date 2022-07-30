package com.codejune.common.handler;

import com.codejune.common.model.Filter;
import com.codejune.common.model.Query;
import com.codejune.jdbc.Table;
import java.util.List;

/**
 * 数据分页处理
 *
 * @author ZJ
 * */
public abstract class DataPageHandler<T> {

    private final int size;

    private final QueryData<T> queryData;

    public DataPageHandler(int size, QueryData<T> queryData) {
        this.size = size;
        this.queryData = queryData;
    }

    @SuppressWarnings("unchecked")
    public DataPageHandler(int size, Table table, Filter filter) {
        this(size, (page, size1) -> (List<T>) table.query(new Query().setPage(page).setSize(size1).setFilter(filter)).getData());
    }

    public DataPageHandler(int size, Table table) {
        this(size, table, null);
    }

    /**
     * 数据处理
     *
     * @param data 数据
     * */
    public abstract void handler(List<T> data);

    /**
     * 执行
     * */
    public final void run() {
        if (queryData == null) {
            return;
        }
        for (int i = 1; ; i++) {
            List<T> data = queryData.query(i, size);
            if (data == null || data.size() == 0) {
                break;
            } else {
                handler(data);
            }
        }
    }

    /**
     * 自定义查询
     *
     * @author ZJ
     * */
    public interface QueryData<T> {

        /**
         * 查询数据
         *
         * @param page 页数
         * @param size 大小
         *
         * @return 查询到的数据
         * */
        List<T> query(int page, int size);

    }

}