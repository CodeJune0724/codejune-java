package com.codejune.core.util;

import com.codejune.core.BaseException;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * PackageUtil
 *
 * @author ZJ
 * */
public final class PackageUtil {

    /**
     * 扫描指定包下的所有类Class
     *
     * @param packageName 包名
     * @param resource 资源类
     *
     * @return 所有类
     * */
    public static List<Class<?>> scan(String packageName, Class<?> resource) {
        if (StringUtil.isEmpty(packageName)) {
            return new ArrayList<>();
        }
        if (resource == null) {
            return new ArrayList<>();
        }
        String packagePath = packageName.replace(".", "/");
        URL url = resource.getResource("/" + packagePath);
        if (url != null) {
            if (url.getProtocol().equals("jar")) {
                return scanJar(url, packagePath);
            } else {
                return scanLocal(url, packagePath);
            }
        } else {
            return new ArrayList<>();
        }
    }


    /**
     * 扫描指定类所在包下的所有类Class
     *
     * @param c 类
     *
     * @return 所有类
     * */
    public static List<Class<?>> scan(Class<?> c) {
        if (c == null) {
            return new ArrayList<>();
        }
        return scan(c.getPackage().getName(), c);
    }

    private static List<Class<?>> scanLocal(URL url, String packagePath) {
        // 返回结果集合
        List<Class<?>> result = new ArrayList<>();

        // 根目录
        String classPath = url.getPath();

        // 递归进行查找类
        findClass(new File(classPath), classPath, packagePath, result);

        return result;
    }

    private static List<Class<?>> scanJar(URL url, String packagePath) {
        List<Class<?>> result = new ArrayList<>();

        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        }

        JarURLConnection jarURLConnection;
        if (urlConnection instanceof JarURLConnection) {
            jarURLConnection = (JarURLConnection) urlConnection;
        } else {
            return null;
        }

        JarFile file;
        try {
            file = jarURLConnection.getJarFile();
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        }
        Enumeration<JarEntry> enumeration = file.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement();

            // 必须是class文件并且是指定包路径下的
            String name = jarEntry.getName();
            if (name.startsWith(packagePath) && name.endsWith(".class")) {
                name = name.replace("/", ".");
                name = name.substring(0, name.length() - 6);
                try {
                    result.add(Class.forName(name));
                } catch (ClassNotFoundException e) {
                    throw new BaseException(e.getMessage());
                }
            }
        }

        return result;
    }

    private static void findClass(File file, String classPath, String packagePath, List<Class<?>> classes) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File file1 : files) {
            // 如果是文件夹，进行递归，是文件进行获取类
            if (file1.isDirectory()) {
                findClass(file1, classPath, packagePath, classes);
            } else {
                // 文件必须是.class结尾
                if (file1.getName().endsWith(".class")) {
                    File classPathFile = new File(classPath);
                    String addClassPath = file1.getPath().replace(classPathFile.getPath(), "");
                    addClassPath = packagePath + addClassPath;
                    addClassPath = addClassPath.replace("\\", ".").replace("/", ".");
                    addClassPath = addClassPath.substring(0, addClassPath.length() - 6);
                    try {
                        classes.add(Class.forName(addClassPath));
                    } catch (ClassNotFoundException e) {
                        throw new BaseException(e.getMessage());
                    }
                }
            }
        }
    }

}