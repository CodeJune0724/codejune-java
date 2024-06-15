package com.codejune.ftp;

import com.codejune.core.BaseException;
import com.codejune.core.os.FileInfo;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.StringUtil;
import com.codejune.ftp.os.File;
import com.codejune.ftp.os.Folder;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
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
                for (FileInfo fileInfo : this.ls(path)) {
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
    protected List<FileInfo> baseLs(String path) {
        try {
            List<FileInfo> result = new ArrayList<>();
            final FTPFile[] ftpFiles = this.ftpClient.listFiles(path);
            for (FTPFile file : ftpFiles) {
                boolean isFile = !file.isDirectory();
                String name = file.getName();
                String filePath = path + "/" + file.getName();
                Date updateTime = file.getTimestamp().getTime();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                FileInfo fileInfo;
                if (isFile) {
                    fileInfo = new File() {
                        @Override
                        public InputStream getInputStream() {
                            try {
                                return ftpClient.retrieveFileStream(filePath);
                            } catch (Exception e) {
                                throw new BaseException(e);
                            }
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
                        public Date getUpdateTime() {
                            return updateTime;
                        }

                        @Override
                        public long getSize() {
                            return file.getSize();
                        }
                    };
                } else {
                    fileInfo = new Folder() {
                        @Override
                        public String getName() {
                            return name;
                        }

                        @Override
                        public String getPath() {
                            return filePath;
                        }

                        @Override
                        public Date getUpdateTime() {
                            return updateTime;
                        }

                        @Override
                        public long getSize() {
                            long result = 0;
                            List<FileInfo> fileInfoList = baseLs(this.getPath());
                            for (FileInfo fileInfo : fileInfoList) {
                                result = result + fileInfo.getSize();
                            }
                            return result;
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