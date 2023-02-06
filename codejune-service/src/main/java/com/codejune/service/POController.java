package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.common.ResponseResult;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public final class POController<T extends BasePO<ID>, ID> {

    private final POService<T, ID> POService;

    public POController(POService<T, ID> poService) {
        if (poService == null) {
            throw new InfoException("poService is null");
        }
        this.POService = poService;
    }

    @PostMapping()
    public ResponseResult save(@RequestBody(required = false) T requestBody, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        if (requestBody != null) {
            requestBody.setId(null);
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    @PostMapping("saveList")
    public ResponseResult saveList(@RequestBody(required = false) List<T> requestBody, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        if (requestBody != null) {
            for (T t : requestBody) {
                t.setId(null);
            }
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    @DeleteMapping("{id}")
    public ResponseResult delete(@PathVariable(required = false) ID id, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        POService.delete(id);
        return ResponseResult.returnTrue();
    }

    @DeleteMapping("deleteList")
    public ResponseResult deleteList(@RequestBody(required = false) List<ID> requestBody, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        if (requestBody != null) {
            for (ID id : requestBody) {
                POService.delete(id);
            }
        }
        return ResponseResult.returnTrue();
    }

    @PutMapping("{id}")
    public ResponseResult update(@PathVariable(required = false) ID id, @RequestBody(required = false) T requestBody, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        if (id == null) {
            throw new InfoException("参数缺失");
        }
        if (requestBody != null) {
            requestBody.setId(id);
        }
        return ResponseResult.returnTrue(POService.save(requestBody));
    }

    @PostMapping("query")
    public ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        return ResponseResult.returnTrue(POService.query(Query.parse(requestBody)));
    }

    @GetMapping("{id}")
    public ResponseResult getDetails(@PathVariable(required = false) ID id, HttpServletRequest httpServletRequest) {
        beforeHandler(httpServletRequest);
        return ResponseResult.returnTrue(POService.getDetails(id));
    }

    /**
     * 前置处理
     *
     * @param httpServletRequest httpServletRequest
     * */
    public void beforeHandler(HttpServletRequest httpServletRequest) {}

}