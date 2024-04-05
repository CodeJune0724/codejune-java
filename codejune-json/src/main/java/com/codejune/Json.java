package com.codejune;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.codejune.common.BaseException;
import com.codejune.common.util.ObjectUtil;
import com.codejune.common.util.StringUtil;
import java.util.List;
import java.util.Map;

/**
 * Json
 *
 * @author ZJ
 * */
public class Json {

    /**
     * 转成json字符串
     *
     * @param Object Object
     *
     * @return json字符串
     * */
    public static String toString(Object Object) {
        if (Object == null) {
            return null;
        }
        if (Object instanceof String) {
            return ObjectUtil.toString(Object);
        }
        try {
            return JSON.toJSONString(Object, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.WriteEnumsUsingName, JSONWriter.Feature.LargeObject);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * json字符串转成对象
     *
     * @param <T> T
     * @param data data
     * @param tClass tClass
     *
     * @return T
     * */
    public static <T> T parse(Object data, Class<T> tClass) {
        if (data == null) {
            return  null;
        }
        try {
            return JSON.parseObject(toString(data), tClass);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 是否是json格式
     *
     * @param data data
     *
     * @return 是否是json格式
     * */
    public static boolean isJson(String data) {
        if (!StringUtil.isEmpty(data)) {
            try {
                Json.parse(data, Map.class);
                return true;
            } catch (Exception e) {
                try {
                    Json.parse(data, List.class);
                    return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

}