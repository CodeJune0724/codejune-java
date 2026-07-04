package com.codejune.uiauto.match;

import com.codejune.uiauto.TwoCoordinate;

/**
 * 匹配配置
 *
 * @author ZJ
 * */
public class MatchConfig {

    private TwoCoordinate range;

    private double similar = 0.9;

    private boolean cvtColor = true;

    public TwoCoordinate getRange() {
        return this.range;
    }

    public void setRange(TwoCoordinate range) {
        this.range = range;
    }

    public double getSimilar() {
        return this.similar;
    }

    public MatchConfig setSimilar(double similar) {
        this.similar = similar;
        return this;
    }

    public boolean isCvtColor() {
        return cvtColor;
    }

    public MatchConfig setCvtColor(boolean cvtColor) {
        this.cvtColor = cvtColor;
        return this;
    }

}