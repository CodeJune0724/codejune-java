package com.codejune.uiauto.match;

import com.codejune.uiauto.Coordinate;

/**
 * 匹配结果
 *
 * @author ZJ
 * */
public final class MatchResult extends Coordinate {

    private final double similar;

    public MatchResult(int x, int y, double similar) {
        super(x, y);
        this.similar = similar;
    }

    public double getSimilar() {
        return this.similar;
    }

}