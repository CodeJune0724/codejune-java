package com.benjaminwan.ocrlibrary;

public class OcrEngine {

    /**
     * 设置线程数
     *
     * @param numThread numThread
     * */
    public native boolean setNumThread(int numThread);

    /**
     * 设置gpu
     *
     * @param gpuIndex gpuIndex
     * */
    public native void setGpuIndex(int gpuIndex);

    /**
     * 初始化模块
     *
     * @param modelsDir 模型路径
     * @param detName detName
     * @param clsName clsName
     * @param recName recName
     * @param keysName keysName
     *
     * @return boolean
     * */
    public native boolean initModels(String modelsDir, String detName, String clsName, String recName, String keysName);

    /**
     * 识别
     *
     * @param input 图片路径
     * @param padding 图像外接白框，用于提升识别率，文字框没有正确框住所有文字时，增加此值。默认50。
     * @param maxSideLen 按图像长边进行总体缩放，放大增加识别耗时但精度更高，缩小减小耗时但精度降低，maxSideLen为0表示不缩放
     * @param boxScoreThresh 文字框置信度门限，文字框没有正确框住所有文字时，减小此值
     * @param boxThresh 同上，自行试验
     * @param unClipRatio 单个文字框大小倍率，越大时单个文字框越大
     * @param doAngle 启用(true)/禁用(false) 文字方向检测，只有图片倒置的情况下(旋转90~270度的图片)，才需要启用文字方向检测，默认关闭
     * @param mostAngle 启用(true)/禁用(false) 角度投票(整张图片以最大可能文字方向来识别)，当禁用文字方向检测时，此项也不起作用，默认关闭
     *
     * @return OcrResult
     * */
    public native OcrResult detect(String input, int padding, int maxSideLen, float boxScoreThresh, float boxThresh, float unClipRatio, boolean doAngle, boolean mostAngle);

}