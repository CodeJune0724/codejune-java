package com.benjaminwan.ocrlibrary;

import java.util.ArrayList;

public class TextBlock {

    private final ArrayList<Point> boxPoint;

    private final float boxScore;

    private final int angleIndex;

    private final float angleScore;

    private final double angleTime;

    private final String text;

    private final float[] charScores;

    private final double crnnTime;

    private final double blockTime;

    public TextBlock(ArrayList<Point> boxPoint, float boxScore, int angleIndex, float angleScore, double angleTime, String text, float[] charScores, double crnnTime, double blockTime) {
        this.boxPoint = boxPoint;
        this.boxScore = boxScore;
        this.angleIndex = angleIndex;
        this.angleScore = angleScore;
        this.angleTime = angleTime;
        this.text = text;
        this.charScores = charScores;
        this.crnnTime = crnnTime;
        this.blockTime = blockTime;
    }

    public ArrayList<Point> getBoxPoint() {
        return boxPoint;
    }

    public float getBoxScore() {
        return boxScore;
    }

    public int getAngleIndex() {
        return angleIndex;
    }

    public float getAngleScore() {
        return angleScore;
    }

    public double getAngleTime() {
        return angleTime;
    }

    public String getText() {
        return text;
    }

    public float[] getCharScores() {
        return charScores;
    }

    public double getCrnnTime() {
        return crnnTime;
    }

    public double getBlockTime() {
        return blockTime;
    }

}