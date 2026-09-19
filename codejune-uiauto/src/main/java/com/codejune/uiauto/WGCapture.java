package com.codejune.uiauto;

import com.codejune.core.BaseException;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * WGCapture
 *
 * @author ZJ
 * */
public class WGCapture implements AutoCloseable {

    private final WinDef.HWND hwnd;

    private long sessionId;

    private BufferedImage reusableImage;

    private int[] imageData;

    private int cachedWidth  = 0;

    private int cachedHeight = 0;

    public WGCapture(WinDef.HWND hwnd) {
        this.hwnd = hwnd;
        this.init();
    }

    @Override
    public void close() {
        long id = this.sessionId;
        this.sessionId = 0;
        if (id != 0) {
            try {
                nativeStopCapture(id);
            } catch (Throwable _) {}
        }
        this.reusableImage = null;
        this.imageData = null;
        this.cachedWidth = 0;
        this.cachedHeight = 0;
    }

    /**
     * 初始化
     * */
    private void init() {
        long pointer = Pointer.nativeValue(hwnd.getPointer());
        this.sessionId = nativeStartCapture(pointer);
        if (this.sessionId == 0) {
            throw new RuntimeException("WGC初始化失败，请确认系统为 Win10 1903+ 且句柄有效");
        }
    }

    /**
     * 截图
     *
     * @return BufferedImage
     */
    public BufferedImage capture() {
        if (this.sessionId == 0) {
            throw new BaseException("WGCapture");
        }
        ByteBuffer byteBuffer = nativeGetFrameBuffer(this.sessionId);
        if (byteBuffer == null) return null;
        int w = nativeGetWidth(this.sessionId);
        int h = nativeGetHeight(this.sessionId);
        if (w <= 0 || h <= 0) return null;
        if (this.reusableImage == null || this.cachedWidth != w || this.cachedHeight != h) {
            if (this.cachedWidth == 0 || this.cachedHeight == 0) {
                this.reusableImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                this.imageData = ((DataBufferInt) this.reusableImage.getRaster().getDataBuffer()).getData();
                this.cachedWidth = w;
                this.cachedHeight = h;
            } else {
                this.close();
                this.init();
                return this.capture();
            }
        }
        final int pixelCount = w * h;
        byteBuffer.rewind();
        for (int i = 0; i < pixelCount; i++) {
            int b = byteBuffer.get() & 0xFF;
            int g = byteBuffer.get() & 0xFF;
            int r = byteBuffer.get() & 0xFF;
            byteBuffer.get();
            this.imageData[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        return this.reusableImage;
    }

    /**
     * 截图
     *
     * @return BufferedImage
     */
    public BufferedImage captureCopy() {
        BufferedImage bufferedImage = this.capture();
        if (bufferedImage == null) return null;
        BufferedImage result = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        result.getGraphics().drawImage(bufferedImage, 0, 0, null);
        return result;
    }

    /**
     * getHwnd
     *
     * @return hwnd
     * */
    public WinDef.HWND getHwnd() {
        return this.hwnd;
    }

    /**
     * 加载dll文件
     *
     * @param dllFile dllFile
     * */
    public static void load(File dllFile) {
        System.load(dllFile.getAbsolutePath());
    }

    private static native long nativeStartCapture(long hwnd);

    private static native ByteBuffer nativeGetFrameBuffer(long sessionId);

    private static native int nativeGetWidth(long sessionId);

    private static native int nativeGetHeight(long sessionId);

    private static native void nativeStopCapture(long sessionId);

}