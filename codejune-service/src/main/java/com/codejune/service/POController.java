package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.common.ResponseResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

public class POController<T extends BasePO<ID>, ID> {

    protected final POService<T, ID> poService;

    public POController(POService<T, ID> poService) {
        if (poService == null) {
            throw new InfoException("poService is null");
        }
        this.poService = poService;
    }

    @PostMapping("query")
    public ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(poService.query(Query.parse(requestBody)));
    }

    @PostMapping("save")
    public ResponseResult save(@RequestBody(required = false) T requestBody) {
        return ResponseResult.returnTrue(poService.save(requestBody));
    }

    @PostMapping("saveList")
    public ResponseResult saveList(@RequestBody(required = false) List<T> requestBody) {
        return ResponseResult.returnTrue(poService.save(requestBody));
    }

    @PostMapping("delete")
    public ResponseResult delete(@RequestBody(required = false) T requestBody) {
        poService.delete(requestBody);
        return ResponseResult.returnTrue();
    }

    @PostMapping("deleteList")
    public ResponseResult deleteList(@RequestBody(required = false) List<T> requestBody) {
        poService.delete(requestBody);
        return ResponseResult.returnTrue();
    }

    @GetMapping("{id}")
    public ResponseResult getDetails(@PathVariable(required = false) ID id) {
        return ResponseResult.returnTrue(poService.getDetails(id));
    }

}