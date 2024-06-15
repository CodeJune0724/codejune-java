package com.codejune.core;

import com.codejune.core.util.ObjectUtil;
import java.util.List;

/**
 * 数据分页处理
 *
 * @author ZJ
 * */
public abstract class DataPageExecutor<T> {

    public DataPageExecutor(int size1, long count) {
        for (int page = 1; ; page++) {
            int currentSize;
            if (count > 0) {
                currentSize = (int) count - ((page - 1) * size1);
                if (currentSize <= 0) {
                    break;
                }
                if (currentSize >= size1) {
                    currentSize = size1;
                }
            } else {
                currentSize = size1;
            }
            List<T> data = query(page, currentSize);
            if (count <= 0 && ObjectUtil.isEmpty(data)) {
                break;
            }
            handler(data);
        }
    }

    public DataPageExecutor(int size) {
        this(size, -1);
    }

    public DataPageExecutor() {
        this(-1);
    }

    /**
     * 数据查询
     *
     * @param page 页数
     * @param size 大小
     *
     * @return 查询到的数据
     * */
    public abstract List<T> query(int page, int size);

    /**
     * 数据处理
     *
     * @param data 数据
     * */
    public abstract void handler(List<T> data);

}