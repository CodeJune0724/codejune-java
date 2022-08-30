package com.codejune.ftp;

import com.codejune.common.os.FileInfo;
import com.codejune.ftp.os.File;
import com.codejune.ftp.os.Folder;
import com.jcraft.jsch.*;
import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.DownloadHandler;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Sftp
 *
 * @author ZJ
 * */
public final class Sftp extends com.codejune.Ftp {

    /**
     * sftp连接
     * */
    private ChannelSftp channelSftp;

    /**
     * session通道
     * */
    private Session session;

    public Sftp(String host, int port, String username, String password) {
        super(host, port, username, password);
    }

    @Override
    protected void connect() {
        try {
            JSch jSch = new JSch();
            this.session = jSch.getSession(this.getUsername(), this.getHost(), this.getPort());
            this.session.setPassword(this.getPassword());
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            this.session.setConfig(properties);
            this.session.connect();
            this.channelSftp = (ChannelSftp) session.openChannel("sftp");
            this.channelSftp.connect();
        }catch (Exception e) {
            throw new InfoException("sftp: " + this.getHost() + ", 连接失败");
        }
    }

    @Override
    public void close() {
        if (this.session != null) {
            this.session.disconnect();
        }

        if (this.channelSftp != null) {
            this.channelSftp.disconnect();
        }
    }

    @Override
    public void upload(String path, String name, String data) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(data.getBytes());
            this.channelSftp.cd(path);
            channelSftp.put(inputStream, name);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void upload(String path, String name, java.io.File file) {
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(file.toPath());
            this.channelSftp.cd(path);
            channelSftp.put(inputStream, name);
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected List<FileInfo> baseLs(String path) {
        try {
            List<FileInfo> result = new ArrayList<>();
            Vector<?> ls = this.channelSftp.ls(path);
            for (Object o : ls) {
                ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry) o;
                SftpATTRS sftpATTRS = lsEntry.getAttrs();
                boolean isFile = !sftpATTRS.isDir();
                String name = lsEntry.getFilename();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                String filePath = path + "/" + name;
                Date updateTime = DateUtil.parse(sftpATTRS.getMtimeString(), "EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                FileInfo fileInfo;
                if (isFile) {
                    fileInfo = new File() {
                        @Override
                        public InputStream getInputStream() {
                            try {
                                return channelSftp.get(filePath);
                            } catch (Exception e) {
                                throw new InfoException(e);
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
                            return sftpATTRS.getSize();
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
            this.channelSftp.cd(path);
            inputStream = this.channelSftp.get(fileName);
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
            this.channelSftp.ls("/");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
