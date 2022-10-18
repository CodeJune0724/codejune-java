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

public class BaseDatabaseController<T extends BasePO> implements DatabaseController {

    protected final DatabaseService<T> databaseService;

    public BaseDatabaseController(DatabaseService<T> databaseService) {
        if (databaseService == null) {
            throw new InfoException("databaseService is null");
        }
        this.databaseService = databaseService;
    }

    @PostMapping("query")
    @Override
    public ResponseResult query(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(null, null, databaseService.query(Query.parse(requestBody)));
    }

    @PostMapping("save")
    @Override
    public ResponseResult save(@RequestBody(required = false) Map<String, Object> requestBody) {
        return ResponseResult.returnTrue(null, null, databaseService.save(MapUtil.transform(requestBody, databaseService.getGenericClass())));
    }

    @PostMapping("saveList")
    @Override
    public ResponseResult saveList(@RequestBody(required = false) List<Object> requestBody) {
        if (requestBody == null) {
            return ResponseResult.returnTrue();
        }
        List<T> tList = new ArrayList<>();
        for (Object o : requestBody) {
            tList.add(ObjectUtil.transform(o, databaseService.getGenericClass()));
        }
        return ResponseResult.returnTrue(null, null, databaseService.save(tList));
    }

    @PostMapping("delete")
    @Override
    public ResponseResult delete(@RequestBody(required = false) Map<String, Object> requestBody) {
        databaseService.delete(MapUtil.transform(requestBody, databaseService.getGenericClass()));
        return ResponseResult.returnTrue();
    }

    @PostMapping("deleteList")
    @Override
    public ResponseResult deleteList(@RequestBody(required = false) List<Object> requestBody) {
        if (requestBody == null) {
            return ResponseResult.returnTrue();
        }
        List<T> tList = new ArrayList<>();
        for (Object o : requestBody) {
            tList.add(ObjectUtil.transform(o, databaseService.getGenericClass()));
        }
        databaseService.delete(tList);
        return ResponseResult.returnTrue();
    }

    @GetMapping("{id}")
    @Override
    public ResponseResult getDetails(@PathVariable(required = false) Object id) {
        return ResponseResult.returnTrue(databaseService.getDetails(id));
    }

}