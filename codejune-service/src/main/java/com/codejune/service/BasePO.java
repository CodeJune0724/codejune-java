package com.codejune.service;

import com.codejune.core.BaseException;
import com.codejune.core.ClassInfo;
import com.codejune.core.classinfo.Field;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础实体
 *
 * @author ZJ
 * */
public abstract class BasePO<ID> {

    @Id
    private ID id;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    /**
     * 获取Id字段
     *
     * @return Id字段
     * */
    public static java.lang.reflect.Field getIdField() {
        List<Field> allFields = new ClassInfo(BasePO.class).getField();
        for (Field field : allFields) {
            if (field.getJavaField().isAnnotationPresent(Id.class)) {
                return field.getJavaField();
            }
        }
        throw new Error("ID未配置");
    }

    /**
     * 获取Column字段
     *
     * @param c c
     *
     * @return 字段
     * */
    public static List<java.lang.reflect.Field> getColumnFields(Class<? extends BasePO<?>> c) {
        if (c == null) {
            throw new BaseException("c is null");
        }
        List<java.lang.reflect.Field> result = new ArrayList<>();
        List<Field> allFields = new ClassInfo(c).getField();
        for (Field field : allFields) {
            if (field.getJavaField().isAnnotationPresent(Column.class)) {
                result.add(field.getJavaField());
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
    public static List<java.lang.reflect.Field> getAllFields(Class<? extends BasePO<?>> c) {
        if (c == null) {
            throw new BaseException("c is null");
        }
        List<java.lang.reflect.Field> result = new ArrayList<>();
        result.add(getIdField());
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
    public static String getTableName(Class<? extends BasePO<?>> c) {
        if (c == null) {
            throw new BaseException("c is null");
        }
        Table table = c.getAnnotation(Table.class);
        if (table == null) {
            throw new BaseException("未配置表名");
        }
        return table.name();
    }

}