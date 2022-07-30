package com.codejune.jdbc.handler;

import com.codejune.common.ClassInfo;
import com.codejune.common.DataType;
import com.codejune.common.classInfo.Field;
import com.codejune.common.handler.KeyHandler;
import com.codejune.common.util.ObjectUtil;
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
public final class ColumnToFieldKeyHandler implements KeyHandler {

    private final Map<String, String> newKeyMap = new HashMap<>();

    public ColumnToFieldKeyHandler(Class<?> c, String idName) {
        if (DataType.toDataType(c) != DataType.OBJECT) {
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

    @Override
    public Object getNewKey(Object key) {
        String result = this.newKeyMap.get(ObjectUtil.toString(key));
        if (StringUtil.isEmpty(result)) {
            return key;
        }
        return result;
    }

}