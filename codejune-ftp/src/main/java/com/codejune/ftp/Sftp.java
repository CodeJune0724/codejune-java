package com.codejune.ftp;

import com.codejune.common.os.FileInfo;
import com.codejune.ftp.os.File;
import com.codejune.ftp.os.Folder;
import com.jcraft.jsch.*;
import com.codejune.common.BaseException;
import com.codejune.common.util.DateUtil;
import com.codejune.common.util.IOUtil;
import com.codejune.common.util.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * Sftp
 *
 * @author ZJ
 * */
public final class Sftp extends com.codejune.Ftp {

    private ChannelSftp channelSftp;

    private Session session;

    public Sftp(String host, int port, String username, String password) {
        super(host, port, username, password);
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

    @Override
    public boolean exist(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        try {
            this.channelSftp.stat(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isFile(String path) {
        if (!exist(path)) {
            return false;
        }
        try {
            this.channelSftp.cd(path);
            return false;
        } catch (Exception e) {
            return true;
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
            this.channelSftp.cd(path);
            channelSftp.put(inputStream, updateFileName);
        } catch (Exception e) {
            throw new BaseException(e);
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
        try (InputStream inputStream = this.channelSftp.get(filePath)) {
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
                this.channelSftp.rm(path);
            } else {
                for (FileInfo fileInfo : this.ls(path)) {
                    delete(fileInfo.getPath());
                }
                this.channelSftp.rmdir(path);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    @Override
    public void createFolder(String path) {
        if (StringUtil.isEmpty(path)) {
            return;
        }
        if (isFolder(path)) {
            return;
        }
        if (isFile(path)) {
            throw new BaseException(path + " is file");
        }

        try {
            this.channelSftp.mkdir(path);
        } catch (Exception e) {
            throw new BaseException(e);
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
    protected void connect() {
        try {
            JSch.setConfig("kex", JSch.getConfig("kex") + ",diffie-hellman-group1-sha1");
            JSch.setConfig("server_host_key", JSch.getConfig("server_host_key") + ",ssh-rsa,ssh-dss");
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
            throw new BaseException("sftp: " + this.getHost() + ", 连接失败");
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
            throw new BaseException(e.getMessage());
        }
    }

}