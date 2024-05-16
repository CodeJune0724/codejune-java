package com.codejune.service;

import com.codejune.common.ResponseResult;
import com.codejune.common.BaseException;
import com.codejune.common.util.ArrayUtil;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import com.codejune.jdbc.Query;
import com.codejune.jdbc.query.Filter;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * POController
 *
 * @author ZJ
 * */
public abstract class POController<T extends BasePO<ID>, ID> {

    /**
     * 获取service
     *
     * @return POService
     * */
    public abstract POService<T, ID> getService();

    @PostMapping()
    public final ResponseResult save(@RequestBody(required = false) Map<String, Object> requestBody) {
        if (requestBody != null) {
            requestBody.put("id", null);
        }
        return ResponseResult.returnTrue(getService().save(ObjectUtil.transform(requestBody, getService().getPOClass())));
    }

    @PostMapping("saveList")
    public final ResponseResult saveList(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(getService().save(
                ArrayUtil.parseList(MapUtil.get(requestBody, "data", List.class), getService().getPOClass()),
                MapUtil.get(requestBody, "filter", Filter.class)
        ));
    }

    @DeleteMapping("{id}")
    public final ResponseResult delete(@PathVariable(name = "id", required = false) ID id) {
        getService().delete(id);
        return ResponseResult.returnTrue();
    }

    @DeleteMapping("deleteList")
    public final ResponseResult deleteList(@RequestBody(required = false) List<ID> requestBody) {
        if (requestBody != null) {
            for (ID id : requestBody) {
                getService().delete(id);
            }
        }
        return ResponseResult.returnTrue();
    }

    @PutMapping("{id}")
    public final ResponseResult update(@PathVariable(name = "id", required = false) ID id, @RequestBody(required = false) Map<String, Object> requestBody) {
        if (id == null) {
            throw new BaseException("参数缺失");
        }
        if (requestBody != null) {
            requestBody.put("id", id);
        }
        return ResponseResult.returnTrue(getService().save(ObjectUtil.transform(requestBody, getService().getPOClass())));
    }

    @PostMapping("query")
    public ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(getService().query(ObjectUtil.transform(requestBody, Query.class)));
    }

    @GetMapping("{id}")
    public final ResponseResult getDetails(@PathVariable(name = "id", required = false) ID id) {
        return ResponseResult.returnTrue(getService().getDetails(id));
    }

}