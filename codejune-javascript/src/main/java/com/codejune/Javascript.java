package com.codejune;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.FileUtil;
import com.codejune.core.util.StringUtil;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import java.io.File;

/**
 * Javascript
 *
 * @author ZJ
 * */
public final class Javascript {

    private final String code;

    public Javascript(String code) {
        this.code = code;
    }

    public Javascript(File file) {
        if (!FileUtil.isFile(file)) {
            throw new BaseException("not file");
        }
        this.code = new com.codejune.core.os.File(file).getData();
    }

    /**
     * 执行
     *
     * @param function 方法名
     * @param param 参数
     *
     * @return 返回结果
     * */
    public String execute(String function, Object... param) {
        try (Context context = Context.create()) {
            String paramString = ArrayUtil.toString(ArrayUtil.asList(param), item -> {
                if (item == null) {
                    return "null";
                }
                if (item instanceof String string) {
                    return "\"" + string + "\"";
                }
                return item.toString();
            }, ", ");
            String jsCode = StringUtil.isEmpty(function) ? this.code : this.code + "\n" + function + "(" + (StringUtil.isEmpty(paramString) ? "" : paramString) + ")";
            Value eval = context.eval("js", jsCode);
            String result = eval.toString();
            return "undefined".equals(result) ? null : result;
        }
    }

    /**
     * 执行
     *
     * @return 返回结果
     * */
    public String execute() {
        return this.execute(null);
    }

}