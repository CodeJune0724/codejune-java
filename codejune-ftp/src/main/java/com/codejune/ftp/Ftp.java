package com.codejune.ftp;

import com.codejune.core.BaseException;
import com.codejune.core.os.FileInfo;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ftp
 *
 * @author ZJ
 */
public final class Ftp extends com.codejune.Ftp {

    /**
     * FTPClient
     * */
    private FTPClient ftpClient;

    public Ftp(String host, int port, String username, String password) {
        super(host, port, username, password);
    }

    @Override
    public boolean isConnected() {
        try {
            this.ftpClient.listFiles();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exist(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        try {
            return this.ftpClient.getStatus(path) != null;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public boolean isFile(String path) {
        try {
            boolean result = this.ftpClient.changeWorkingDirectory(path);
            this.ftpClient.changeWorkingDirectory("/");
            return !result;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public boolean isFolder(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        return !this.isFile(path);
    }

    @Override
    public void upload(String path, String updateFileName, InputStream inputStream) {
        boolean isCloseInputStream = false;
        try {
            if (inputStream == null) {
                inputStream = new ByteArrayInputStream("".getBytes());
                isCloseInputStream = true;
            }
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.storeFile(path + "/" + updateFileName, inputStream);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        } finally {
            if (isCloseInputStream) {
                IOUtil.close(inputStream);
            }
        }
    }

    @Override
    public void download(String filePath, Consumer<InputStream> listener) {
        if (!isFile(filePath)) {
            throw new BaseException(filePath + " is not file");
        }
        if (listener == null) {
            listener = data -> {};
        }
        try (InputStream inputStream = this.ftpClient.retrieveFileStream(filePath)) {
            listener.accept(inputStream);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public void delete(String path) {
        if (!exist(path)) {
            return;
        }
        try {
            if (isFile(path)) {
                this.ftpClient.deleteFile(path);
            } else {
                for (FileInfo<InputStream> fileInfo : this.ls(path)) {
                    delete(fileInfo.getPath());
                }
                this.ftpClient.removeDirectory(path);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void createFolder(String path) {
        if (isFile(path)) {
            throw new BaseException(path + " is file");
        }
        try {
            this.ftpClient.mkd(path);
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.ftpClient.logout();
            this.ftpClient.disconnect();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    protected void connect() {
        try {
            this.ftpClient = new FTPClient();
            this.ftpClient.connect(this.getHost(), this.getPort());
            this.ftpClient.login(this.getUsername(), this.getPassword());
            this.ftpClient.enterLocalPassiveMode();
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    protected List<FileInfo<InputStream>> baseLs(String path) {
        try {
            List<FileInfo<InputStream>> result = new ArrayList<>();
            final FTPFile[] ftpFiles = this.ftpClient.listFiles(path);
            for (FTPFile file : ftpFiles) {
                boolean isFile = !file.isDirectory();
                String name = file.getName();
                String filePath = path + "/" + file.getName();
                LocalDateTime updateTime = ObjectUtil.parse(file.getTimestamp().getTime(), LocalDateTime.class);
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                FileInfo<InputStream> fileInfo;
                if (isFile) {
                    fileInfo = new FileInfo<>() {
                        @Override
                        public InputStream getData() {
                            try {
                                return ftpClient.retrieveFileStream(filePath);
                            } catch (Exception e) {
                                throw new BaseException(e);
                            }
                        }

                        @Override
                        public boolean isFile() {
                            return true;
                        }

                        @Override
                        public String getName() {
                            return name;
                        }

                        @Override
                        public String getPath() {
                            return filePath;
                        }

                        @Override
                        public LocalDateTime getUpdateTime() {
                            return updateTime;
                        }

                        @Override
                        public long getSize() {
                            return file.getSize();
                        }
                    };
                } else {
                    fileInfo = new FileInfo<>() {
                        @Override
                        public String getName() {
                            return name;
                        }

                        @Override
                        public String getPath() {
                            return filePath;
                        }

                        @Override
                        public LocalDateTime getUpdateTime() {
                            return updateTime;
                        }

                        @Override
                        public long getSize() {
                            long result = 0;
                            List<FileInfo<InputStream>> fileInfoList = Ftp.this.baseLs(this.getPath());
                            for (FileInfo<InputStream> fileInfo : fileInfoList) {
                                result = result + fileInfo.getSize();
                            }
                            return result;
                        }

                        @Override
                        public InputStream getData() {
                            return null;
                        }

                        @Override
                        public boolean isFile() {
                            return false;
                        }
                    };
                }
                result.add(fileInfo);
            }
            return result;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

}