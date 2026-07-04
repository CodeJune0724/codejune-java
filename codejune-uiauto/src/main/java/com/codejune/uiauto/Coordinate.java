package com.codejune.uiauto;

/**
 * 坐标
 *
 * @author ZJ
 * */
public class Coordinate {

    private final int x;

    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
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

    @Override
    public String toString() {
        return "x: " + this.x + ", y: " + this.y;
    }

}