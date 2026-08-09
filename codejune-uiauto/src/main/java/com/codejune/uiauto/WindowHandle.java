package com.codejune.uiauto;

import com.codejune.Shell;
import com.codejune.core.BaseException;
import com.codejune.core.os.OSType;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.core.util.ThreadUtil;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 窗口句柄
 *
 * @author ZJ
 * */
public final class WindowHandle {

    private final int id;

    private final String name;

    private final String process;

    private final WinDef.HWND hwnd;

    private static final User32 USER_32 = User32.INSTANCE;

    private static final User32Extra USER32_EXTRA = Native.load("user32", User32Extra.class);

    private WindowHandle(int id, String name, String process, WinDef.HWND hwnd) {
        this.id = id;
        this.name = name;
        this.process = process;
        this.hwnd = hwnd;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getProcess() {
        return this.process;
    }

    public WinDef.HWND getHwnd() {
        return this.hwnd;
    }

    /**
     * 获取位置
     *
     * @return TwoCoordinate
     * */
    public TwoCoordinate getPosition() {
        WinDef.RECT rect = new WinDef.RECT();
        USER_32.GetWindowRect(this.hwnd, rect);
        return new TwoCoordinate(rect.left, rect.top, rect.right, rect.bottom);
    }

    /**
     * 修改窗口大小
     *
     * @param width 宽度
     * @param height 高度
     * */
    public void setSize(int width, int height) {
        USER_32.SetWindowPos(this.hwnd, null, 0, 0, width, height, User32.SWP_NOMOVE | User32.SWP_NOZORDER);
    }

    /**
     * 是否有效
     *
     * @return 是否有效
     * */
    public boolean effective() {
        return USER_32.IsWindowVisible(this.hwnd);
    }

    /**
     * 截图
     *
     * @param cutTop 裁剪上面
     * @param cutLeft 裁剪左边
     *
     * @return BufferedImage
     * */
    public BufferedImage capture(int cutTop, int cutLeft) {
        WinDef.RECT rect = new WinDef.RECT();
        USER_32.GetWindowRect(this.hwnd, rect);
        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;
        WinDef.HDC screenDC = USER_32.GetDC(null);
        if (screenDC == null) {
            throw new BaseException("GetDC失败");
        }
        WinDef.HDC memDC = null;
        WinDef.HBITMAP hBitmap = null;
        WinNT.HANDLE oldBitmap = null;
        try {
            memDC = GDI32.INSTANCE.CreateCompatibleDC(screenDC);
            if (memDC == null) {
                throw new BaseException("CreateCompatibleDC失败");
            }
            hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(screenDC, width, height);
            if (hBitmap == null) {
                throw new BaseException("CreateCompatibleBitmap失败");
            }
            oldBitmap = GDI32.INSTANCE.SelectObject(memDC, hBitmap);
            if (oldBitmap == null) {
                throw new BaseException("SelectObject失败");
            }
            int flags = 0;
            flags |= 0x00000002;
            boolean printSuccess = USER32_EXTRA.PrintWindow(hwnd, memDC, flags);
            if (!printSuccess) {
                WinDef.HDC windowDC = USER_32.GetDC(hwnd);
                if (windowDC != null) {
                    if (!GDI32.INSTANCE.BitBlt(memDC, 0, 0, width, height, windowDC, 0, 0, 0x00CC0020)) {
                        throw new BaseException("BitBlt失败");
                    }
                }
                USER_32.ReleaseDC(hwnd, windowDC);
            }
            WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
            bmi.bmiHeader.biWidth = width;
            bmi.bmiHeader.biHeight = -height;
            bmi.bmiHeader.biPlanes = 1;
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
            bmi.bmiHeader.biSize = bmi.bmiHeader.size();
            int bytesPerPixel = 4;
            int bufferSize = width * height * bytesPerPixel;
            try (Memory memory = new Memory(bufferSize)) {
                int result = GDI32.INSTANCE.GetDIBits(memDC, hBitmap, 0, height, memory, bmi, WinGDI.DIB_RGB_COLORS);
                if (result == 0) {
                    throw new BaseException("GetDIBits失败");
                }
                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                int[] pixels = new int[width * height];
                memory.read(0, pixels, 0, pixels.length);
                bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
                if (cutTop != 0 || cutLeft != 0) {
                    BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth() + cutLeft, bufferedImage.getHeight() + cutTop, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics2D = null;
                    try {
                        graphics2D = newBufferedImage.createGraphics();
                        graphics2D.setColor(new Color(0, 255, 0, 255));
                        graphics2D.fillRect(0, 0, newBufferedImage.getWidth(), newBufferedImage.getHeight());
                        graphics2D.drawImage(bufferedImage,
                                Math.max(cutLeft, 0), Math.max(cutTop, 0), newBufferedImage.getWidth(), newBufferedImage.getHeight(),
                                cutLeft > 0 ? 0 : -cutLeft, cutTop > 0 ? 0 : -cutTop, bufferedImage.getWidth(), bufferedImage.getHeight(),
                                null
                        );
                    } finally {
                        if (graphics2D != null) {
                            graphics2D.dispose();
                        }
                    }
                    bufferedImage = newBufferedImage;
                }
                return bufferedImage;
            }
        } finally {
            if (oldBitmap != null) {
                GDI32.INSTANCE.SelectObject(memDC, oldBitmap);
            }
            if (memDC != null) {
                GDI32.INSTANCE.DeleteDC(memDC);
            }
            if (hBitmap != null) {
                GDI32.INSTANCE.DeleteObject(hBitmap);
            }
            USER_32.ReleaseDC(null, screenDC);
        }
    }

    /**
     * 截图
     *
     * @return BufferedImage
     * */
    public BufferedImage capture() {
        return this.capture(0, 0);
    }

    /**
     * 点击
     *
     * @param x x
     * @param y y
     * @param delay 延迟释放
     * */
    public void click(int x, int y, int delay) {
        if (x < 0 || y < 0) {
            return;
        }
        if (delay < 0) {
            delay = 0;
        }
        WinDef.POINT point = new WinDef.POINT(x - 8, y);
        int lParam = (point.y << 16) | (point.x & 0xFFFF);
        USER_32.SendMessage(this.hwnd, 0x0201, new WinDef.WPARAM(1), new WinDef.LPARAM(lParam));
        if (delay > 0) {
            ThreadUtil.sleep(delay);
        }
        USER_32.SendMessage(this.hwnd, 0x0202, new WinDef.WPARAM(0), new WinDef.LPARAM(lParam));
    }

    /**
     * 点击
     *
     * @param x x
     * @param y y
     * */
    public void click(int x, int y) {
        this.click(x, y, 0);
    }

    /**
     * 点击
     *
     * @param coordinate coordinate
     * @param delay 延迟释放
     * */
    public void click(Coordinate coordinate, int delay) {
        if (coordinate == null) {
            return;
        }
        this.click(coordinate.getX(), coordinate.getY(), delay);
    }

    /**
     * 点击
     *
     * @param coordinate coordinate
     * */
    public void click(Coordinate coordinate) {
        this.click(coordinate, 0);
    }

    /**
     * 挪动
     *
     * @param x x
     * @param y y
     * */
    public void move(int x, int y) {
        TwoCoordinate twoCoordinate = this.getPosition();
        User32.INSTANCE.MoveWindow(this.hwnd, x, y, twoCoordinate.getWidth(), twoCoordinate.getHeight(), true);
    }

    /**
     * 获取所有窗口句柄
     *
     * @return List<WindowHandle>
     * */
    public static List<WindowHandle> get() {
        return baseGet(null, null, null);
    }

    /**
     * 通过pid获取窗口句柄
     *
     * @param pid pid
     *
     * @return WindowHandle
     * */
    public static WindowHandle getByPid(int pid) {
        List<WindowHandle> windowHandleList = baseGet(pid, null, null);
        if (ObjectUtil.isEmpty(windowHandleList)) {
            return null;
        }
        return windowHandleList.getFirst();
    }

    /**
     * 通过name获取窗口句柄
     *
     * @param name name
     *
     * @return WindowHandle
     * */
    public static List<WindowHandle> getByName(String name) {
        return baseGet(null, name, null);
    }

    /**
     * 通过processName获取窗口句柄
     *
     * @param processName processName
     *
     * @return WindowHandle
     * */
    public static List<WindowHandle> getByProcessName(String processName) {
        return baseGet(null, null, processName);
    }

    private static List<WindowHandle> baseGet(Integer queryPid, String queryName, String queryProcessName) {
        List<WindowHandle> result = new ArrayList<>();
        USER_32.EnumWindows((hwnd, _) -> {
            if (!USER_32.IsWindowVisible(hwnd)) {
                return true;
            }
            IntByReference intByReference = new IntByReference();
            USER_32.GetWindowThreadProcessId(hwnd, intByReference);
            int pid = intByReference.getValue();
            if (queryPid != null && pid != queryPid) {
                return true;
            }
            char[] titleBuffer = new char[1024];
            USER_32.GetWindowText(hwnd, titleBuffer, titleBuffer.length);
            String name = Native.toString(titleBuffer);
            if (StringUtil.isEmpty(name)) {
                return true;
            }
            if (!StringUtil.isEmpty(queryName) && !ObjectUtil.equals(name, queryName)) {
                return true;
            }
            if (!StringUtil.isEmpty(queryProcessName) && !ObjectUtil.equals(getProcessName(pid), queryProcessName)) {
                return true;
            }
            result.add(new WindowHandle(pid, name, getProcess(pid), hwnd));
            return true;
        }, null);
        return result;
    }

    private static String getProcess(int pid) {
        if (OSType.getCurrentOSType() == OSType.WINDOWS_7) {
            String commandResult = Shell.fastCommand("wmic process get ProcessId,CommandLine /format:csv | findstr " + pid);
            if (commandResult == null) {
                return null;
            }
            for (String item : commandResult.split("\n")) {
                if (!item.endsWith("," + pid)) {
                    continue;
                }
                return item.replace("," + pid, "");
            }
            return null;
        } else {
            String commandResult = Shell.fastCommand("powershell -NoProfile -Command \"Get-CimInstance Win32_Process -Filter ProcessId=" + pid + " | Select-Object ProcessId, CommandLine | Format-Table -Wrap -AutoSize\"");
            if (commandResult == null) {
                return null;
            }
            commandResult = commandResult.replace("\n         ", "");
            for (String item : commandResult.split("\n")) {
                item = item.trim();
                if (!item.startsWith(pid + "")) {
                    continue;
                }
                return item.replace(pid + " ", "");
            }
            return null;
        }
    }

    private static String getProcessName(int pid) {
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        if (snapshot == WinNT.INVALID_HANDLE_VALUE) {
            return null;
        }
        try {
            Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
            if (Kernel32.INSTANCE.Process32First(snapshot, processEntry)) {
                do {
                    if (processEntry.th32ProcessID.intValue() == pid) {
                        return Native.toString(processEntry.szExeFile);
                    }
                } while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry));
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return null;
    }

    private interface User32Extra extends User32 {
        boolean PrintWindow(HWND hwnd, HDC hdcBlt, int nFlags);
    }

}