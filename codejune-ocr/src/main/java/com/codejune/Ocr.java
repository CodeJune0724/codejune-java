package com.codejune;

import com.benjaminwan.ocrlibrary.OcrEngine;
import com.benjaminwan.ocrlibrary.Point;
import com.benjaminwan.ocrlibrary.TextBlock;
import com.codejune.core.BaseException;
import com.codejune.core.Image;
import com.codejune.core.util.FileUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.ocr.OcrResult;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class Ocr {

    private final OcrEngine ocrEngine;

    public Ocr(File dll, String path, String det, String cls, String rec, String keys) {
        try {
            System.load(dll.getAbsolutePath());
            this.ocrEngine = new OcrEngine();
            this.setNumThread(1);
            this.ocrEngine.initModels(
                    path,
                    det,
                    cls,
                    rec,
                    keys
            );
        } catch (Throwable e) {
            throw new BaseException(e);
        }
    }

    /**
     * 设置线程
     *
     * @param numThread numThread
     * */
    public void setNumThread(int numThread) {
        this.ocrEngine.setNumThread(numThread);
    }

    /**
     * 识别
     *
     * @param file file
     * @param colorHandler 颜色处理
     * @param colorHandlerFile 颜色处理后的文件
     *
     * @return List<OcrResult>
     * */
    public List<OcrResult> ocr(File file, Function<Integer, Integer> colorHandler, File colorHandlerFile) {
        if (!FileUtil.isFile(file)) {
            return null;
        }
        if (colorHandler != null) {
            try (Image image = new Image(file)) {
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        Integer newRGB = colorHandler.apply(image.getRGB(x, y));
                        if (newRGB == null) {
                            continue;
                        }
                        image.setRGB(x, y, newRGB);
                    }
                }
                image.save(colorHandlerFile);
            }
        }
        com.benjaminwan.ocrlibrary.OcrResult ocrResult = this.ocrEngine.detect(
                colorHandler != null ? colorHandlerFile.getAbsolutePath() : file.getAbsolutePath(),
                50,
                0,
                0.5f,
                0.3f,
                1.6f,
                false,
                false
        );
        List<OcrResult> result = new ArrayList<>();
        for (TextBlock textBlock : ocrResult.getTextBlocks()) {
            String text = textBlock.getText();
            if (StringUtil.isEmpty(text)) {
                continue;
            }
            ArrayList<Point> boxPoint = textBlock.getBoxPoint();
            Point onePoint = boxPoint.getFirst();
            Point towPoint = boxPoint.get(2);
            int left = onePoint.getX();
            int right = towPoint.getX();
            int top = onePoint.getY();
            int bottom = towPoint.getY();
            int x = ((right - left) / 2) + left;
            int y = ((bottom - top) / 2) + top;
            result.add(new OcrResult(text, x, y));
        }
        result.sort(Comparator.comparingInt(OcrResult::getY));
        return result;
    }

    public List<OcrResult> ocr(File file) {
        return this.ocr(file, null, null);
    }


}