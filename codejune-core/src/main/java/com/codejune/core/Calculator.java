package com.codejune.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算器
 *
 * @author ZJ
 * */
public final class Calculator {

    private double count = 0;

    public Calculator(Number init) {
        if (init == null) {
            return;
        }
        this.count = Double.parseDouble(String.valueOf(init));
    }

    public Calculator() {
        this(0);
    }

    /**
     * 加法
     *
     * @param data data
     *
     * @return this
     * */
    public Calculator add(Number data) {
        if (data == null) {
            return this;
        }
        this.count = new BigDecimal(Double.toString(this.count)).add(new BigDecimal(String.valueOf(data))).doubleValue();
        return this;
    }

    /**
     * 减法
     *
     * @param data data
     *
     * @return this
     * */
    public Calculator subtract(Number data) {
        if (data == null) {
            return this;
        }
        this.count = new BigDecimal(Double.toString(this.count)).subtract(new BigDecimal(String.valueOf(data))).doubleValue();
        return this;
    }

    /**
     * 乘法
     *
     * @param data data
     *
     * @return this
     * */
    public Calculator multiply(Number data) {
        if (data == null) {
            return this;
        }
        this.count = new BigDecimal(Double.toString(this.count)).multiply(new BigDecimal(String.valueOf(data))).doubleValue();
        return this;
    }

    /**
     * 除法
     *
     * @param data data
     * @param scale 精确位数
     *
     * @return this
     */
    public Calculator divide(Number data, int scale) {
        if (data == null) {
            return this;
        }
        if (scale < 0) {
            throw new BaseException("精确位数必须大于等于0");
        }
        this.count = new BigDecimal(Double.toString(this.count)).divide(new BigDecimal(String.valueOf(data)), scale, RoundingMode.HALF_UP).doubleValue();
        return this;
    }

    /**
     * 除法
     *
     * @param data data
     *
     * @return this
     */
    public Calculator divide(Number data) {
        return this.divide(data, 2);
    }

    /**
     * 保留小数位
     *
     * @param scale 小数位
     *
     * @return this
     * */
    public Calculator scale(int scale) {
        if (scale < 0) {
            throw new BaseException("精确位数必须大于等于0");
        }
        this.count = new BigDecimal(Double.toString(this.count)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        return this;
    }

    /**
     * 计算
     *
     * @return 合
     * */
    public double count() {
        return this.count;
    }

    @Override
    public String toString() {
        return Double.toString(this.count());
    }

}