package com.codejune.service;

import com.codejune.common.ResponseResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

public interface POController<T extends BasePO<ID>, ID> {

    @PostMapping()
    ResponseResult save(@RequestBody(required = false) T requestBody);

    @PostMapping("saveList")
    ResponseResult saveList(@RequestBody(required = false) List<T> requestBody);

    @DeleteMapping("{id}")
    ResponseResult delete(@PathVariable(required = false) ID id);

    @DeleteMapping("deleteList")
    ResponseResult deleteList(@RequestBody(required = false) List<ID> requestBody);

    @PutMapping("{id}")
    ResponseResult update(@PathVariable(required = false) ID id, @RequestBody(required = false) T requestBody);

    @PostMapping("query")
    ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody);

    @GetMapping("{id}")
    ResponseResult getDetails(@PathVariable(required = false) ID id);

}