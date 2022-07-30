package com.codejune.ftp;

import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.DownloadHandler;
import com.codejune.common.model.FileInfo;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    protected void connect() {
        try {
            this.ftpClient = new FTPClient();
            this.ftpClient.connect(this.getHost(), this.getPort());
            this.ftpClient.login(this.getUsername(), this.getPassword());
            this.ftpClient.enterLocalPassiveMode();
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            this.ftpClient.logout();
            this.ftpClient.disconnect();
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public void upload(String path, String name, String data) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(data.getBytes());
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.storeFile(path + "/" + name, inputStream);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
        }
    }

    @Override
    public void upload(String path, String name, File file) {
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(file.toPath());
            this.ftpClient.enterLocalPassiveMode();
            this.ftpClient.storeFile(path + "/" + name, inputStream);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
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
                Date updateTime = file.getTimestamp().getTime();
                result.add(new FileInfo(isFile, path, name, updateTime) {
                    @Override
                    public long getSize() {
                        if (isFile()) {
                            return file.getSize();
                        } else {
                            long result = 0L;
                            List<FileInfo> ls1 = baseLs(getAbsolutePath());
                            for (FileInfo fileInfo : ls1) {
                                result = result + fileInfo.getSize();
                            }
                            return result;
                        }
                    }

                    @Override
                    public String getData() {
                        InputStream inputStream = null;
                        try {
                            if (!file.isDirectory()) {
                                inputStream = ftpClient.retrieveFileStream(path + "/" + file.getName());
                            }
                            return IOUtil.toString(inputStream);
                        } catch (Exception e) {
                            throw new InfoException(e.getMessage());
                        } finally {
                            IOUtil.close(inputStream);
                            if (inputStream != null) {
                                try {
                                    ftpClient.completePendingCommand();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
            List<FileInfo> resultTemp = new ArrayList<>(result);
            result = new ArrayList<>();
            for (FileInfo fileInfo : resultTemp) {
                if (!".".equals(fileInfo.getName()) && !"..".equals(fileInfo.getName())) {
                    result.add(fileInfo);
                }
            }
            return result;
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    @Override
    public void download(String path, String fileName, DownloadHandler downloadHandler) {
        if (StringUtil.isEmpty(path) || StringUtil.isEmpty(fileName)) {
            throw new InfoException("参数缺失，下载失败");
        }
        if (downloadHandler == null) {
            downloadHandler = inputStream -> {};
        }
        InputStream inputStream = null;
        try {
            inputStream = this.ftpClient.retrieveFileStream(new File(path, fileName).getAbsolutePath());
            downloadHandler.download(inputStream);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(inputStream);
        }
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

}
