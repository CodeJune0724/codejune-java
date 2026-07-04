package com.codejune.uiauto;

/**
 * 二维坐标
 *
 * @author ZJ
 * */
public class TwoCoordinate extends Coordinate {

    private final int x2;

    private final int y2;

    public TwoCoordinate(int x, int y, int x2, int y2) {
        super(x, y);
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX2() {
        return this.x2;
    }

    public int getY2() {
        return this.y2;
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     * */
    public int getWidth() {
        return this.x2 - this.getX();
    }

    /**
     * 获取高度
     *
     * @return 高度
     * */
    public int getHeight() {
        return this.y2 - this.getY();
    }

    @Override
    public String toString() {
        return "{\"x\":" + this.getX() + ",\"y\":" + this.getY() + ",\"x2\":" + this.getX2() + ",\"y2\":" + this.getY2() + "}";
    }

}