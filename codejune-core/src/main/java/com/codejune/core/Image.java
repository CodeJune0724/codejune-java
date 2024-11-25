package com.codejune.core;

import com.codejune.core.util.FileUtil;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Image
 *
 * @author ZJ
 * */
public final class Image implements Closeable {

    private final BufferedImage bufferedImage;

    public Image(int width, int height) {
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public Image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public Image(File file) {
        try {
            this.bufferedImage = ImageIO.read(file);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    public Image(InputStream inputStream) {
        try {
            this.bufferedImage = ImageIO.read(inputStream);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     * */
    public int getWidth() {
        return this.bufferedImage.getWidth();
    }

    /**
     * 获取高度
     *
     * @return 宽度
     * */
    public int getHeight() {
        return this.bufferedImage.getHeight();
    }

    /**
     * 裁切
     *
     * @param x x
     * @param y y
     * @param width width
     * @param height height
     *
     * @return Image
     * */
    public Image cut(int x, int y, int width, int height) {
        return new Image(this.bufferedImage.getSubimage(x, y, width, height));
    }

    /**
     * 画
     *
     * @param bufferedImage bufferedImage
     * @param x x
     * @param y y
     * */
    public void draw(BufferedImage bufferedImage, int x, int y) {
        Graphics graphics = this.bufferedImage.getGraphics();
        graphics.drawImage(bufferedImage, x, y, null);
        graphics.dispose();
    }

    /**
     * 画
     *
     * @param image image
     * @param x x
     * @param y y
     * */
    public void draw(Image image, int x, int y) {
        this.draw(image.bufferedImage, x, y);
    }

    /**
     * 保存
     *
     * @param file file
     *
     * @return File
     * */
    public File save(File file) {
        if (!FileUtil.isFile(file)) {
            throw new BaseException("not file");
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            ImageIO.write(this.bufferedImage, new com.codejune.core.os.File(file).getSuffix(), fileOutputStream);
            return file;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    /**
     * 保存
     *
     * @param outputStream outputStream
     * */
    public void save(OutputStream outputStream, String format) {
        try {
            ImageIO.write(this.bufferedImage, format, outputStream);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.bufferedImage.flush();
        } catch (Exception ignored) {}
    }

}