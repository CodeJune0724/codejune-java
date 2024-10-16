package com.codejune.service.handler;

import com.codejune.core.ClassInfo;
import com.codejune.core.Data;
import com.codejune.core.classinfo.Field;
import com.codejune.core.util.StringUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * FieldToColumnKeyHandler
 *
 * @author ZJ
 * */
public final class FieldToColumnHandler implements Function<String, String> {

    private final Map<String, String> newKeyMap = new HashMap<>();

    public FieldToColumnHandler(Class<?> c) {
        if (!Data.isObject(c)) {
            return;
        }
        List<Field> fields = new ClassInfo(c).getField();
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
            newKeyMap.put(key, newKey);
        }
    }

    @Override
    public String apply(String key) {
        String result = this.newKeyMap.get(key);
        if (StringUtil.isEmpty(result)) {
            return null;
        }
        return result;
    }

}