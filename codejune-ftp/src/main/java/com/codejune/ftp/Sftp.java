package com.codejune.ftp;

import com.jcraft.jsch.*;
import com.codejune.common.exception.InfoException;
import com.codejune.common.handler.DownloadHandler;
import com.codejune.common.model.FileInfo;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.*;
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
    public void upload(String path, String name, File file) {
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
                if (".".equals(lsEntry.getFilename()) || "..".equals(lsEntry.getFilename())) {
                    continue;
                }
                SftpATTRS attrs = lsEntry.getAttrs();
                boolean isFile = !attrs.isDir();
                String name = lsEntry.getFilename();
                Date updateTime = DateUtil.parse(attrs.getMtimeString(), "EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                result.add(new FileInfo(isFile, path, name, updateTime) {
                    @Override
                    public long getSize() {
                        if (isFile()) {
                            return lsEntry.getAttrs().getSize();
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
                            if (!lsEntry.getAttrs().isDir()) {
                                inputStream = channelSftp.get(path + "/" + lsEntry.getFilename());
                            }
                            return IOUtil.toString(inputStream);
                        } catch (Exception e) {
                          throw new InfoException(e.getMessage());
                        } finally {
                            IOUtil.close(inputStream);
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
