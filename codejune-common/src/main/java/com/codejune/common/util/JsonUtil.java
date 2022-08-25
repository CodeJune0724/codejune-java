package com.codejune.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.codejune.common.exception.InfoException;
import java.util.List;
import java.util.Map;

/**
 * JsonUtil
 *
 * @author ZJ
 * */
public class JsonUtil {

    /**
     * 转成json字符串
     *
     * @param Object Object
     *
     * @return json字符串
     * */
    public static String toJsonString(Object Object) {
        try {
            return JSON.toJSONString(Object, SerializerFeature.WriteMapNullValue);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
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
            return JSON.parseObject(ObjectUtil.toString(data), tClass, Feature.OrderedField);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
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
                JsonUtil.parse(data, Map.class);
                return true;
            } catch (Exception e) {
                try {
                    JsonUtil.parse(data, List.class);
                    return true;
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

}