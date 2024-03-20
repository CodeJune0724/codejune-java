package com.codejune.common.util;

import com.codejune.common.BaseException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtil {

    /**
     * 正则查找
     *
     * @param regex 正则表达式
     * @param data 数据
     *
     * @return 查到到的数据
     * */
    public static List<String> find(String regex, String data) {
        if (regex == null) {
            regex = "";
        }
        if (data == null) {
            data = "";
        }
        try {
            List<String> result = new ArrayList<>();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                int groupCount = matcher.groupCount();
                for (int i = 0; i <= groupCount; i++) {
                    result.add(matcher.group(i));
                }
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    /**
     * 正则查找
     *
     * @param regex 正则表达式
     * @param data 数据
     * @param index index
     *
     * @return 查到到的数据
     * */
    public static String find(String regex, String data, int index) {
        List<String> list = find(regex, data);
        if (ObjectUtil.isEmpty(list)) {
            return null;
        }
        if (list.size() <= index) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 是否匹配到
     *
     * @param regex regex
     * @param data 数据
     *
     * @return 是否匹配到
     * */
    public static boolean test(String regex, String data) {
        return !ObjectUtil.isEmpty(find(regex, data));
    }

    /**
     * 转义字符
     *
     * @param data data
     *
     * @return 转义后的数据
     * */
    public static String escape(String data) {
        Set<Character> characterSet = Collections.synchronizedSet(new HashSet<>(Arrays.asList(
                '\\',
                '.',
                '$',
                '&',
                '+',
                '*',
                '?',
                '(',
                ')',
                '[',
                ']',
                '{',
                '}',
                '|',
                '^'
        )));
        if (StringUtil.isEmpty(data)) {
            return data;
        }
        StringBuilder stringBuilder = new StringBuilder(data.length() * 2);
        CharacterIterator iterator = new StringCharacterIterator(data);
        char ch = iterator.current();
        while (CharacterIterator.DONE != ch) {
            if (characterSet.contains(ch)) {
                stringBuilder.append('\\');
            }
            stringBuilder.append(ch);
            ch = iterator.next();
        }
        return stringBuilder.toString();
    }

}