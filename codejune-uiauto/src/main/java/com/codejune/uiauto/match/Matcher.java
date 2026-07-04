package com.codejune.uiauto.match;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.FileUtil;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.uiauto.RGB;
import com.codejune.uiauto.TwoCoordinate;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * 匹配器
 *
 * @author ZJ
 * */
public final class Matcher {

    /**
     * init
     *
     * @param opencvDll opencvDll
     * */
    public static void init(File opencvDll) {
        if (!FileUtil.isFile(opencvDll)) {
            throw new BaseException(opencvDll + " not file");
        }
        System.load(opencvDll.getAbsolutePath());
    }

    /**
     * 匹配图片
     *
     * @param originBufferedImage 原图
     * @param templateBufferedImage 模板
     * @param matchConfig matchConfig
     *
     * @return MatchResult
     * */
    public static MatchResult matchImage(BufferedImage originBufferedImage, List<BufferedImage> templateBufferedImage, MatchConfig matchConfig) {
        if (originBufferedImage == null || ObjectUtil.isEmpty(templateBufferedImage)) {
            return null;
        }
        if (matchConfig == null) {
            matchConfig = new MatchConfig();
        }
        TwoCoordinate range = matchConfig.getRange();
        Mat originMat;
        try {
            if (range != null) {
                originBufferedImage = originBufferedImage.getSubimage(
                        range.getX(),
                        range.getY(),
                        range.getX2() - range.getX(),
                        range.getY2() - range.getY()
                );
            }
            originMat = bufferedImageToMat(originBufferedImage, matchConfig.isCvtColor());
        } catch (Exception e) {
            return null;
        } finally {
            originBufferedImage.flush();
        }
        try {
            for (BufferedImage bufferedImage : templateBufferedImage) {
                Mat templateMat = bufferedImageToMat(bufferedImage, matchConfig.isCvtColor());
                try {
                    Mat resultMat = null;
                    try {
                        resultMat = new Mat(originMat.cols() - templateMat.cols() + 1, originMat.rows() - templateMat.rows() + 1, CvType.CV_32FC1);
                        Imgproc.matchTemplate(originMat, templateMat, resultMat, Imgproc.TM_CCOEFF_NORMED);
                        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(resultMat);
                        if (minMaxLocResult.maxVal >= matchConfig.getSimilar()) {
                            int x = (int) minMaxLocResult.maxLoc.x;
                            int y = (int) minMaxLocResult.maxLoc.y;
                            x = x + templateMat.width() / 2;
                            y = y + templateMat.height() / 2;
                            return new MatchResult(
                                    range != null ? range.getX() + x : x,
                                    range != null ? range.getY() + y : y,
                                    minMaxLocResult.maxVal
                            );
                        }
                    } finally {
                        if (resultMat != null) {
                            resultMat.release();
                        }
                    }
                } finally {
                    templateMat.release();
                }
            }
            return null;
        } finally {
            originMat.release();
        }
    }

    /**
     * 匹配图片
     *
     * @param originBufferedImage 原图
     * @param templateBufferedImage 模板
     * @param matchConfig matchConfig
     *
     * @return MatchResult
     * */
    public static MatchResult matchImage(BufferedImage originBufferedImage, BufferedImage templateBufferedImage, MatchConfig matchConfig) {
        return matchImage(originBufferedImage, ArrayUtil.asList(templateBufferedImage), matchConfig);
    }

    /**
     * 匹配颜色
     *
     * @param originBufferedImage 原图
     * @param templateRGB templateRGB
     * @param matchConfig matchConfig
     *
     * @return MatchResult
     * */
    public static MatchResult matchColor(BufferedImage originBufferedImage, RGB templateRGB, MatchConfig matchConfig) {
        if (originBufferedImage == null || templateRGB == null) {
            return null;
        }
        if (matchConfig == null) {
            matchConfig = new MatchConfig();
        }
        try {
            TwoCoordinate range = matchConfig.getRange();
            if (range == null) {
                range = new TwoCoordinate(0, 0, originBufferedImage.getWidth() - 1, originBufferedImage.getHeight() - 1);
            }
            for (int i = range.getX(); i <= range.getX2(); i++) {
                for (int j = range.getY(); j <= range.getY2(); j++) {
                    RGB originRGB = new RGB(originBufferedImage.getRGB(i, j));
                    if (compareRGB(originRGB, templateRGB, matchConfig)) {
                        return new MatchResult(i, j, matchConfig.getSimilar());
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            originBufferedImage.flush();
        }
    }

    /**
     * 转成BufferedImage
     *
     * @param object object
     *
     * @return BufferedImage
     * */
    public static BufferedImage parseBufferedImage(Object object) {
        switch (object) {
            case null -> {
                return null;
            }
            case InputStream inputStream -> {
                try {
                    return ImageIO.read(inputStream);
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            }
            case File file -> {
                try (InputStream inputStream = IOUtil.getInputStream(file)) {
                    return ImageIO.read(inputStream);
                } catch (Exception e) {
                    throw new BaseException(e);
                }
            }
            default -> {
            }
        }
        throw new BaseException(object + " not parse");
    }

    /**
     * bufferedImageToMat
     *
     * @param bufferedImage bufferedImage
     * @param cvtColor cvtColor
     *
     * @return Mat
     * */
    private static Mat bufferedImageToMat(BufferedImage bufferedImage, boolean cvtColor) {
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        try {
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            newBufferedImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
            byte[] newBufferedImageByte = ((DataBufferByte) newBufferedImage.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, newBufferedImageByte);
            if (cvtColor) {
                Mat result = new Mat();
                Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2GRAY);
                return result;
            } else {
                return mat;
            }
        } finally {
            if (cvtColor) {
                mat.release();
            }
        }
    }

    /**
     * RGB比对算法
     *
     * @param originRgb 原RGB
     * @param templateRGB 模板RGB
     * @param matchConfig matchConfig
     *
     * @return 比对是否成功
     * */
    private static boolean compareRGB(RGB originRgb, RGB templateRGB, MatchConfig matchConfig) {
        if (originRgb == null || templateRGB == null) {
            return false;
        }
        if (matchConfig == null) {
            matchConfig = new MatchConfig();
        }
        int offset = matchConfig.getSimilar() >= 1 ? 0 : 100 - (int) (100 * matchConfig.getSimilar());
        return (templateRGB.getR() < 0 || (templateRGB.getR() <= originRgb.getR() + offset && templateRGB.getR() >= originRgb.getR() - offset))
                &&
                (templateRGB.getG() < 0 || (templateRGB.getG() <= originRgb.getG() + offset && templateRGB.getG() >= originRgb.getG() - offset))
                &&
                (templateRGB.getB() < 0 || (templateRGB.getB() <= originRgb.getB() + offset && templateRGB.getB() >= originRgb.getB() - offset));
    }

}