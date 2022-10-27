package com.codejune.common.util;

import com.codejune.common.exception.InfoException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipUtil
 *
 * @author ZJ
 * */
public final class ZipUtil {

    /**
     * 压缩
     *
     * @param dirs 源文件或者文件夹
     * @param outFile 输出文件
     * */
    public static void zip(List<String> dirs, File outFile) {
        if (ObjectUtil.isEmpty(dirs)) {
            return;
        }
        if (outFile == null) {
            throw new InfoException("outFile is null");
        }
        if (outFile.exists()) {
            FileUtil.delete(outFile);
        }
        File parent = outFile.getParentFile();
        if (!parent.exists()) {
            boolean mkdirs = parent.mkdirs();
            if (!mkdirs) {
                throw new InfoException("创建文件夹失败");
            }
        }
        try {
            zip(dirs, outFile.getAbsolutePath());
        } catch (Exception e) {
            throw new InfoException(e.getMessage());
        }
    }

    private static void zip(List<String> dirs, String outDir) {
        OutputStream out = null;
        ZipOutputStream zos = null;
        try {
            out = Files.newOutputStream(Paths.get(outDir));
            zos = new ZipOutputStream(out);
            List<File> sourceFileList = new ArrayList<>();
            for (String dir : dirs) {
                File sourceFile = new File(dir);
                sourceFileList.add(sourceFile);
            }
            compress(sourceFileList, zos);
        } catch (Exception e) {
            throw new InfoException(e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            IOUtil.close(out);
        }

    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) {
        FileInputStream in = null;

        try {
            byte[] buf = new byte[2 * 1024];
            if (sourceFile.isFile()) {
                zos.putNextEntry(new ZipEntry(name));
                int len;
                in = new FileInputStream(sourceFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                // Complete the entry
                zos.closeEntry();
                in.close();
            } else {
                File[] listFiles = sourceFile.listFiles();
                if (listFiles == null || listFiles.length == 0) {
                    if (KeepDirStructure) {
                        zos.putNextEntry(new ZipEntry(name + "/"));
                        zos.closeEntry();
                    }

                } else {
                    for (File file : listFiles) {
                        if (KeepDirStructure) {
                            compress(file, zos, name + "/" + file.getName(),
                                    KeepDirStructure);
                        } else {
                            compress(file, zos, file.getName(), KeepDirStructure);
                        }

                    }
                }
            }
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(in);
        }
    }

    private static void compress(List<File> sourceFileList, ZipOutputStream zos) {
        FileInputStream in = null;

        try {
            byte[] buf = new byte[2 * 1024];
            for (File sourceFile : sourceFileList) {
                String name = sourceFile.getName();
                if (sourceFile.isFile()) {
                    zos.putNextEntry(new ZipEntry(name));
                    int len;
                    in = new FileInputStream(sourceFile);
                    while ((len = in.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                    in.close();
                } else {
                    File[] listFiles = sourceFile.listFiles();
                    if (listFiles == null || listFiles.length == 0) {
                        zos.putNextEntry(new ZipEntry(name + "/"));
                        zos.closeEntry();

                    } else {
                        for (File file : listFiles) {
                            compress(file, zos, name + "/" + file.getName(),
                                    true);

                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new InfoException(e.getMessage());
        } finally {
            IOUtil.close(in);
        }
    }

}