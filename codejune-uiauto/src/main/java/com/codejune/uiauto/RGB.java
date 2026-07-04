package com.codejune.uiauto;

/**
 * RGB
 *
 * @author ZJ
 * */
public final class RGB {

    private final int r;

    private final int g;

    private final int b;

    public RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public RGB(int grb) {
        this((grb >> 16) & 0xff, (grb >> 8) & 0xff, grb & 0xff);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

}