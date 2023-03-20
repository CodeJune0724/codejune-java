package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.common.ResponseResult;
import java.util.List;
import java.util.Map;

public final class BasePOController<T extends BasePO<ID>, ID> {

    public ResponseResult save(T requestBody, POService<T, ID> POService) {
        if (requestBody != null) {
            requestBody.setId(null);
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    public ResponseResult saveList(List<T> requestBody, POService<T, ID> POService) {
        if (requestBody != null) {
            for (T t : requestBody) {
                t.setId(null);
            }
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    public ResponseResult delete(ID id, POService<T, ID> POService) {
        POService.delete(id);
        return ResponseResult.returnTrue();
    }

    public ResponseResult deleteList(List<ID> requestBody, POService<T, ID> POService) {
        if (requestBody != null) {
            for (ID id : requestBody) {
                POService.delete(id);
            }
        }
        return ResponseResult.returnTrue();
    }

    public ResponseResult update(ID id, T requestBody, POService<T, ID> POService) {
        if (id == null) {
            throw new InfoException("参数缺失");
        }
        if (requestBody != null) {
            requestBody.setId(id);
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    public ResponseResult query(Map<String, Object> requestBody, POService<T, ID> POService) {
        return ResponseResult.returnTrue(POService.query(Query.parse(requestBody)));
    }

    public ResponseResult getDetails(ID id, POService<T, ID> POService) {
        return ResponseResult.returnTrue(POService.getDetails(id));
    }

}