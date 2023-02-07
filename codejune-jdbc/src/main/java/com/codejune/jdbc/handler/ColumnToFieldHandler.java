package com.codejune.jdbc.handler;

import com.codejune.common.ClassInfo;
import com.codejune.common.DataType;
import com.codejune.common.classInfo.Field;
import com.codejune.common.util.StringUtil;
import javax.persistence.Column;
import javax.persistence.Id;
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

    public ColumnToFieldHandler(Class<?> c, String idName) {
        if (DataType.parse(c) != DataType.OBJECT) {
            return;
        }
        List<Field> fields = new ClassInfo(c).getFields();
        for (Field field : fields) {
            String key = field.getName();
            String newKey;
            if (field.isAnnotation(Id.class)) {
                newKey = idName;
            } else if (field.isAnnotation(Column.class)) {
                newKey = field.getAnnotation(Column.class).name();
            } else {
                newKey = key;
            }
            newKeyMap.put(newKey, key);
        }
    }

    public String handler(String key) {
        String result = this.newKeyMap.get(key);
        if (StringUtil.isEmpty(result)) {
            return key;
        }
        return result;
    }

}