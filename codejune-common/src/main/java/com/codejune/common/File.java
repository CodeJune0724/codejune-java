package com.codejune.common;

import com.codejune.common.exception.InfoException;
import com.codejune.common.model.FileInfo;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件
 *
 * @author ZJ
 * */
public final class File extends FileInfo {

    private java.io.File file;

    public File(java.io.File file, FileType fileType) {
        super(create(file, fileType));
        this.file = file;
    }

    public File(String path, FileType fileType) {
        this(new java.io.File(path), fileType);
    }

    public File(java.io.File file) {
        this(file, null);
    }

    public File(String path) {
        this(path, null);
    }

    /**
     * 删除
     * */
    public void delete() {
        if (isFile()) {
            if (!file.delete()) {
                throw new InfoException("删除文件失败");
            }
        } else {
            List<File> files = files();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            if (!this.file.delete()) {
                throw new InfoException("删除文件夹失败");
            }
        }
    }

    /**
     * 获取文件夹中的所有文件
     *
     * @return 文件夹下的所有文件
     * */
    public List<File> files() {
        List<File> result;
        if (isDir()) {
            result = new ArrayList<>();
            java.io.File[] files = this.file.listFiles();
            if (files == null) {
                return result;
            }
            for (java.io.File file : files) {
                result.add(new File(file));
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * 父级
     *
     * @return 父级
     * */
    public File parent() {
        return new File(this.file.getParentFile());
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * @param append 是否追加
     * */
    public void setData(InputStream inputStream, boolean append) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(this.file, append);
            IOUtil.write(outputStream, inputStream);
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(outputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param inputStream inputStream
     * */
    public void setData(InputStream inputStream) {
        setData(inputStream, false);
    }

    /**
     * 写入数据
     *
     * @param data data
     * @param append 是否追加
     * */
    public void setData(String data, boolean append) {
        if (StringUtil.isEmpty(data)) {
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(data.getBytes());
            setData(inputStream, append);
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param data data
     * */
    public void setData(String data) {
        setData(data, false);
    }

    /**
     * 写入数据
     *
     * @param multipartFile multipartFile
     * */
    public void setData(MultipartFile multipartFile) {
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            setData(inputStream);
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 写入数据
     *
     * @param bytes bytes
     * */
    public void setData(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        InputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            setData(inputStream);
        } finally {
            IOUtil.close(inputStream);
        }
    }

    /**
     * 复制文件
     *
     * @param copyPath 复制路径
     *
     * @return File
     * */
    public File copy(String copyPath) {
        if (StringUtil.isEmpty(copyPath)) {
            return null;
        }
        java.io.File copyPathFile = new java.io.File(copyPath);
        if (copyPathFile.exists() && copyPathFile.isFile()) {
            throw new InfoException(copyPath + " is not dir");
        }
        if (!copyPathFile.exists() && !copyPathFile.mkdirs()) {
            throw new InfoException("创建文件夹失败");
        }

        // 先删除，再生成
        java.io.File file = new java.io.File(copyPath, getName());
        if (file.exists()) {
            new File(file).delete();
        }

        File result;
        if (isFile()) {
            result = new File(file, FileType.FILE);
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(this.file);
                result.setData(fileInputStream);
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
            finally {
                IOUtil.close(fileInputStream);
            }
        } else {
            result = createDir(file.getAbsolutePath());
            List<File> files = files();
            if (files != null) {
                for (File f : files) {
                    f.copy(file.getAbsolutePath());
                }
            }
        }

        return result;
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
            throw new InfoException("重命名失败");
        }
        this.file = newFile.getAbsoluteFile();
    }

    @Override
    public long getSize() {
        long result = 0L;
        if (isFile()) {
            result = result + this.file.length();
        } else {
            List<File> files = files();
            if (files != null) {
                for (File file : files) {
                    result = result + file.getSize();
                }
            }
        }
        return result;
    }

    @Override
    public String getData() {
        StringBuilder result = new StringBuilder();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = Files.newInputStream(file.toPath());
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            char[] chars = new char[1024];
            int i = bufferedReader.read(chars);
            while (i != -1) {
                result.append(new String(chars, 0, i));
                i = bufferedReader.read(chars);
            }
            return result.toString();
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
            IOUtil.close(inputStreamReader);
            IOUtil.close(bufferedReader);
        }
    }

    private static java.io.File create(java.io.File file, FileType fileType) {
        if (fileType == null) {
            if (!file.exists()) {
                throw new InfoException("文件不存在，请指定类型");
            }
        }
        if (fileType == FileType.FILE) {
            createFile(file);
        }
        if (fileType == FileType.DIR) {
            createDir(file.getAbsolutePath());
        }
        return file;
    }

    private static void createFile(java.io.File file) {
        if (file == null) {
            throw new InfoException("file is null");
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new InfoException("file is exists");
            }
        } else {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new InfoException("file create fail");
                }
            }
            try {
                if (!file.createNewFile()) {
                    throw new InfoException("file create fail");
                }
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }

        }
    }

    private static File createDir(String path) {
        if (StringUtil.isEmpty(path)) {
            throw new InfoException("path is null");
        }
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            if (file.isFile()) {
                throw new InfoException("file is exists");
            }
        } else {
            try {
                if (!file.mkdirs()) {
                    throw new InfoException("创建文件夹失败：" + path);
                }
            } catch (Exception e) {
                throw new InfoException(e.getMessage());
            }
        }
        return new File(file);
    }

    /**
     * 文件类型
     *
     * @author ZJ
     * */
    public enum FileType {

        /**
         * 文件
         * */
        FILE,

        /**
         * 文件夹
         * */
        DIR

    }

}
