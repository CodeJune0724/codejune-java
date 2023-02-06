package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.common.ResponseResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

public final class POController<T extends BasePO<ID>, ID> {

    @PostMapping()
    public ResponseResult save(@RequestBody(required = false) T requestBody, POService<T, ID> POService) {
        if (requestBody != null) {
            requestBody.setId(null);
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    @PostMapping("saveList")
    public ResponseResult saveList(@RequestBody(required = false) List<T> requestBody, POService<T, ID> POService) {
        if (requestBody != null) {
            for (T t : requestBody) {
                t.setId(null);
            }
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    @DeleteMapping("{id}")
    public ResponseResult delete(@PathVariable(required = false) ID id, POService<T, ID> POService) {
        POService.delete(id);
        return ResponseResult.returnTrue();
    }

    @DeleteMapping("deleteList")
    public ResponseResult deleteList(@RequestBody(required = false) List<ID> requestBody, POService<T, ID> POService) {
        if (requestBody != null) {
            for (ID id : requestBody) {
                POService.delete(id);
            }
        }
        return ResponseResult.returnTrue();
    }

    @PutMapping("{id}")
    public ResponseResult update(@PathVariable(required = false) ID id, @RequestBody(required = false) T requestBody, POService<T, ID> POService) {
        if (id == null) {
            throw new InfoException("参数缺失");
        }
        if (requestBody != null) {
            requestBody.setId(id);
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    @PostMapping("query")
    public ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody, POService<T, ID> POService) {
        return ResponseResult.returnTrue(POService.query(Query.parse(requestBody)));
    }

    @GetMapping("{id}")
    public ResponseResult getDetails(@PathVariable(required = false) ID id, POService<T, ID> POService) {
        return ResponseResult.returnTrue(POService.getDetails(id));
    }

}