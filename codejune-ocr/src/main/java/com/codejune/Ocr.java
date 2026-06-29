package com.codejune;

import com.benjaminwan.ocrlibrary.OcrEngine;
import com.benjaminwan.ocrlibrary.Point;
import com.benjaminwan.ocrlibrary.TextBlock;
import com.codejune.core.BaseException;
import com.codejune.core.util.FileUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.ocr.OcrResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Ocr {

    private final OcrEngine ocrEngine;

    public Ocr(String ncnnPath) {
        if (!FileUtil.isFolder(new File(ncnnPath))) {
            throw new BaseException("ncnnPath is null");
        }
        try {
            System.load(new File(ncnnPath, "RapidOcrNcnn.dll").getAbsolutePath());
            this.ocrEngine = new OcrEngine();
            this.ocrEngine.setNumThread(1);
            this.ocrEngine.initModels(
                    ncnnPath,
                    "det",
                    "ch_ppocr_mobile_v2.0_cls_infer",
                    "rec",
                    "ppocr_keys_v1.txt"
            );
        } catch (Throwable e) {
            throw new BaseException(e);
        }
    }

    /**
     * 识别
     *
     * @param file file
     *
     * @return List<OcrResult>
     * */
    public synchronized List<OcrResult> ocr(File file) {
        if (!FileUtil.isFile(file)) {
            return null;
        }
        com.benjaminwan.ocrlibrary.OcrResult ocrResult = this.ocrEngine.detect(
                file.getAbsolutePath(),
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

}