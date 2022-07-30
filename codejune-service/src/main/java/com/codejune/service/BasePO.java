package com.codejune.service;

import com.codejune.common.ClassInfo;
import com.codejune.common.classInfo.Field;
import com.codejune.common.exception.ErrorException;
import com.codejune.common.exception.InfoException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础实体
 *
 * @author ZJ
 * */
public abstract class BasePO {

    @Id(name = "ID")
    private Object id;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    /**
     * 获取Id字段
     *
     * @return Id字段
     * */
    public static java.lang.reflect.Field idField() {
        List<Field> allFields = new ClassInfo(BasePO.class).getFields();
        for (Field field : allFields) {
            if (field.getOriginField().isAnnotationPresent(Id.class)) {
                return field.getOriginField();
            }
        }
        throw new ErrorException("ID未配置");
    }

    /**
     * 获取id名称
     *
     * @return id名称
     * */
    public static String idName() {
        return idField().getAnnotation(Id.class).name();
    }

    /**
     * 获取Column字段
     *
     * @param c c
     *
     * @return 字段
     * */
    public static List<java.lang.reflect.Field> getColumnFields(Class<? extends BasePO> c) {
        if (c == null) {
            throw new InfoException("c is null");
        }
        List<java.lang.reflect.Field> result = new ArrayList<>();
        List<Field> allFields = new ClassInfo(c).getFields();
        for (Field field : allFields) {
            if (field.getOriginField().isAnnotationPresent(Column.class)) {
                result.add(field.getOriginField());
            }
        }
        return result;
    }

    /**
     * 获取所有字段
     *
     * @param c c
     *
     * @return 字段
     * */
    public static List<java.lang.reflect.Field> getAllFields(Class<? extends BasePO> c) {
        if (c == null) {
            throw new InfoException("c is null");
        }
        List<java.lang.reflect.Field> result = new ArrayList<>();
        result.add(idField());
        result.addAll(getColumnFields(c));
        return result;
    }

    /**
     * 获取表名
     *
     * @param c c
     *
     * @return 表名
     * */
    public static String tableName(Class<? extends BasePO> c) {
        if (c == null) {
            throw new InfoException("c is null");
        }
        Table table = c.getAnnotation(Table.class);
        if (table == null) {
            throw new InfoException("未配置表名");
        }
        return table.name();
    }

}