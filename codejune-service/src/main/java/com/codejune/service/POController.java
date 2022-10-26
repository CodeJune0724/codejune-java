package com.codejune.service;

import com.codejune.common.exception.InfoException;
import com.codejune.jdbc.Query;
import com.codejune.common.ResponseResult;
import com.codejune.common.util.MapUtil;
import com.codejune.common.util.ObjectUtil;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class POController<T extends BasePO> {

    protected final POService<T> poService;

    public POController(POService<T> poService) {
        if (poService == null) {
            throw new InfoException("poService is null");
        }
        this.poService = poService;
    }

    @PostMapping("query")
    public ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(null, null, poService.query(Query.parse(requestBody)));
    }

    @PostMapping("save")
    public ResponseResult save(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(null, null, poService.save(MapUtil.transform(requestBody, poService.getGenericClass())));
    }

    @PostMapping("saveList")
    public ResponseResult saveList(@RequestBody(required = false) List<Object> requestBody) {
        if (requestBody == null) {
            return ResponseResult.returnTrue();
        }
        List<T> tList = new ArrayList<>();
        for (Object o : requestBody) {
            tList.add(ObjectUtil.transform(o, poService.getGenericClass()));
        }
        return ResponseResult.returnTrue(null, null, poService.save(tList));
    }

    @PostMapping("delete")
    public ResponseResult delete(@RequestBody(required = false) Map<String, Object> requestBody) {
        poService.delete(MapUtil.transform(requestBody, poService.getGenericClass()));
        return ResponseResult.returnTrue();
    }

    @PostMapping("deleteList")
    public ResponseResult deleteList(@RequestBody(required = false) List<Object> requestBody) {
        if (requestBody == null) {
            return ResponseResult.returnTrue();
        }
        List<T> tList = new ArrayList<>();
        for (Object o : requestBody) {
            tList.add(ObjectUtil.transform(o, poService.getGenericClass()));
        }
        poService.delete(tList);
        return ResponseResult.returnTrue();
    }

    @GetMapping("{id}")
    public ResponseResult getDetails(@PathVariable(required = false) Object id) {
        return ResponseResult.returnTrue(poService.getDetails(id));
    }

}