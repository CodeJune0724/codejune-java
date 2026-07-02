package com.codejune.ocr;

import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.util.List;

/**
 * OcrResult
 *
 * @author ZJ
 * */
public final class OcrResult {

    private String text;

    private final int x;

    private final int y;

    public OcrResult(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    /**
     * getText
     *
     * @return text
     * */
    public String getText() {
        return this.text;
    }

    /**
     * getX
     *
     * @return x
     * */
    public int getX() {
        return this.x;
    }

    /**
     * getY
     *
     * @return y
     * */
    public int getY() {
        return this.y;
    }

    /**
     * 纠错
     *
     * @param data 数据字典
     * */
    public void correct(List<String> data) {
        if (StringUtil.isEmpty(this.text)) {
            return;
        }
        boolean exist = false;
        for (String item : data) {
            if (ObjectUtil.equals(this.text, item)) {
                exist = true;
                break;
            }
        }
        if (exist) {
            return;
        }
        int count = 1;
        for (String item : data) {
            if (StringUtil.isEmpty(item)) {
                continue;
            }
            int correctNumber = 0;
            for (int i = 0; i < item.length(); i++) {
                if (i >= this.text.length()) {
                    break;
                }
                if (ObjectUtil.equals(item.substring(i, i + 1), this.text.substring(i, i + 1))) {
                    correctNumber = correctNumber + 1;
                }
            }
            if (item.length() > count && correctNumber >= item.length() - count) {
                this.text = item;
                return;
            }
        }
    }

}