package com.codejune.ocr;

public final class OcrResult {

    private final String text;

    private final int x;

    private final int y;

    public OcrResult(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public String getText() {
        return this.text;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

}