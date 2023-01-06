package com.codejune.common;

import com.codejune.common.exception.InfoException;
import com.codejune.common.util.ObjectUtil;
import java.util.List;

/**
 * 数据分页处理
 *
 * @author ZJ
 * */
public abstract class DataPageExecutor<T> {

    private final int size;

    private final long count;

    public DataPageExecutor(int size, long count) {
        if (size <= 0) {
            throw new InfoException("size <= 0");
        }
        this.size = size;
        this.count = count;
    }

    public DataPageExecutor(int size) {
        this(size, -1);
    }

    /**
     * 数据查询
     *
     * @param page 页数
     * @param size 大小
     *
     * @return 查询到的数据
     * */
    public abstract List<T> queryData(int page, int size);
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
        for (int i = 1; ; i++) {
            int size;
            if (count > 0) {
                size = (int) count - ((i - 1) * this.size);
                if (size <= 0) {
                    break;
                }
                if (size >= this.size) {
                    size = this.size;
                }
            } else {
                size = this.size;
            }
            List<T> data = queryData(i, size);
            if (count <= 0 && ObjectUtil.isEmpty(data)) {
                break;
            }
            handler(data);
        }
    }

}