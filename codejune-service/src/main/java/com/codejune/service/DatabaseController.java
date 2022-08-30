package com.codejune.service;

import com.codejune.common.ResponseResult;
import java.util.List;
import java.util.Map;

public interface DatabaseController {

    /**
     * 查询
     *
     * @param requestBody requestBody
     *
     * @return ResponseResult
     * */
    ResponseResult query(Map<String, Object> requestBody);

    /**
     * 保存
     *
     * @param requestBody requestBody
     *
     * @return ResponseResult
     * */
    ResponseResult save(Map<String, Object> requestBody);

    /**
     * 保存多个
     *
     * @param requestBody requestBody
     *
     * @return ResponseResult
     * */
    ResponseResult saveList(List<Object> requestBody);

    /**
     * 删除
     *
     * @param requestBody requestBody
     *
     * @return ResponseResult
     * */
    ResponseResult delete(Map<String, Object> requestBody);

    /**
     * 删除多个
     *
     * @param requestBody requestBody
     *
     * @return ResponseResult
     * */
    ResponseResult deleteList(List<Object> requestBody);

    /**
     * 获取详情
     * */
    ResponseResult getDetails(Object id);

}