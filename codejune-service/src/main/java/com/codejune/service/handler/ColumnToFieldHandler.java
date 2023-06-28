package com.codejune.service.handler;

import com.codejune.common.ClassInfo;
import com.codejune.common.Data;
import com.codejune.common.classinfo.Field;
import com.codejune.common.util.StringUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ColumnToFieldKeyHandler
 *
 * @author ZJ
 * */
public final class ColumnToFieldHandler {

    private final Map<String, String> newKeyMap = new HashMap<>();

    public ColumnToFieldHandler(Class<?> c) {
        if (!Data.isObject(c)) {
            return;
        }
        List<Field> fields = new ClassInfo(c).getFields();
        for (Field field : fields) {
            String key = field.getName();
            String newKey;
            if (field.isAnnotation(Id.class)) {
                newKey = "ID";
            } else if (field.isAnnotation(Column.class)) {
                newKey = field.getAnnotation(Column.class).name();
            } else {
                continue;
            }
            newKeyMap.put(newKey, key);
        }
    }

    public String handler(String key) {
        String result = this.newKeyMap.get(key);
        if (StringUtil.isEmpty(result)) {
            return null;
        }
        return result;
    }

}