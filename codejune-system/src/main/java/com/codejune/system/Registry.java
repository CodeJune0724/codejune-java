package com.codejune.system;

import com.codejune.core.BaseException;
import com.codejune.core.util.ArrayUtil;
import com.codejune.core.util.ObjectUtil;
import com.codejune.core.util.StringUtil;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import java.util.ArrayList;
import java.util.List;

/**
 * 注册表
 *
 * @author ZJ
 * */
public final class Registry {

    private static final Registry REGISTRY = new Registry();

    private Registry() {}

    /**
     * 添加
     *
     * @param path path
     * @param key key
     * @param value value
     * */
    public void add(String path, String key, Object value) {
        WinReg.HKEY hkey = this.getHkey(path);
        if (hkey == null) {
            throw new BaseException("WinReg.HKEY not found");
        }
        path = this.parsePath(path);
        Advapi32Util.registryCreateKey(hkey, path);
        switch (value) {
            case null -> Advapi32Util.registrySetStringValue(hkey, path, key, null);
            case String s -> Advapi32Util.registrySetStringValue(hkey, path, key, s);
            case Integer integer -> Advapi32Util.registrySetIntValue(hkey, path, key, integer);
            case Long l -> Advapi32Util.registrySetLongValue(hkey, path, key, l);
            case Byte[] byteValue -> {
                byte[] binaryData = new byte[byteValue.length];
                for (int i = 0; i < byteValue.length; i++) {
                    binaryData[i] = byteValue[i];
                }
                Advapi32Util.registrySetBinaryValue(hkey, path, key, binaryData);
            }
            default -> Advapi32Util.registrySetStringValue(hkey, path, key, ObjectUtil.toString(value));
        }
    }

    /**
     * 删除
     *
     * @param path path
     * @param key key
     * */
    public void delete(String path, String key) {
        WinReg.HKEY hkey = this.getHkey(path);
        if (hkey == null) {
            throw new BaseException("WinReg.HKEY not found");
        }
        path = this.parsePath(path);
        Advapi32Util.registryDeleteValue(hkey, path, key);
    }

    /**
     * 删除
     *
     * @param path path
     * */
    public void delete(String path) {
        WinReg.HKEY hkey = this.getHkey(path);
        if (hkey == null) {
            throw new BaseException("WinReg.HKEY not found");
        }
        path = this.parsePath(path);
        String[] subKeyNames = Advapi32Util.registryGetKeys(hkey, path);
        for (String subKeyName : subKeyNames) {
            String subKeyPath = path + "\\" + subKeyName;
            delete(subKeyPath);
        }
        Advapi32Util.registryDeleteKey(hkey, path);
    }

    /**
     * 查询
     *
     * @param path 路径
     * @param key 键
     * @param aClass 类型
     * @param <T> T
     *
     * @return 值
     * */
    public <T> T query(String path, String key, Class<T> aClass) {
        WinReg.HKEY hkey = this.getHkey(path);
        if (hkey == null) {
            throw new BaseException("WinReg.HKEY not found");
        }
        Object result = Advapi32Util.registryGetValue(
                hkey,
                this.parsePath(path),
                key
        );
        return ObjectUtil.parse(result, aClass);
    }

    public String query(String path, String key) {
        return this.query(path, key, String.class);
    }

    public String query(String path) {
        return this.query(path, null, String.class);
    }

    /**
     * 获取子目录
     *
     * @param path path
     *
     * @return 所有键
     * */
    public List<String> getChildPath(String path) {
        WinReg.HKEY hkey = this.getHkey(path);
        if (hkey == null) {
            throw new BaseException("WinReg.HKEY not found");
        }
        return ArrayUtil.asList(Advapi32Util.registryGetKeys(hkey, this.parsePath(path)));
    }

    /**
     * 获取子目录
     *
     * @param path path
     *
     * @return 所有键
     * */
    public List<String> getKeyList(String path) {
        WinReg.HKEY hkey = this.getHkey(path);
        if (hkey == null) {
            throw new BaseException("WinReg.HKEY not found");
        }
        return new ArrayList<>(Advapi32Util.registryGetValues(hkey, this.parsePath(path)).keySet());
    }

    /**
     * 获取Hkey
     *
     * @param path path
     *
     * @return WinReg.HKEY
     * */
    private WinReg.HKEY getHkey(String path) {
        if (StringUtil.isEmpty(path)) {
            return null;
        }
        if (path.startsWith("HKEY_CLASSES_ROOT")) {
            return WinReg.HKEY_CLASSES_ROOT;
        }
        if (path.startsWith("HKEY_CURRENT_USER")) {
            return WinReg.HKEY_CURRENT_USER;
        }
        if (path.startsWith("HKEY_LOCAL_MACHINE")) {
            return WinReg.HKEY_LOCAL_MACHINE;
        }
        if (path.startsWith("HKEY_USERS")) {
            return WinReg.HKEY_USERS;
        }
        if (path.startsWith("HKEY_PERFORMANCE_DATA")) {
            return WinReg.HKEY_PERFORMANCE_DATA;
        }
        if (path.startsWith("HKEY_PERFORMANCE_TEXT")) {
            return WinReg.HKEY_PERFORMANCE_TEXT;
        }
        if (path.startsWith("HKEY_PERFORMANCE_NLSTEXT")) {
            return WinReg.HKEY_PERFORMANCE_NLSTEXT;
        }
        if (path.startsWith("HKEY_CURRENT_CONFIG")) {
            return WinReg.HKEY_CURRENT_CONFIG;
        }
        if (path.startsWith("HKEY_DYN_DATA")) {
            return WinReg.HKEY_DYN_DATA;
        }
        if (path.startsWith("HKEY_CURRENT_USER_LOCAL_SETTINGS")) {
            return WinReg.HKEY_CURRENT_USER_LOCAL_SETTINGS;
        }
        return null;
    }

    /**
     * parsePath
     *
     * @param path path
     *
     * @return path
     * */
    private String parsePath(String path) {
        if (StringUtil.isEmpty(path)) {
            return path;
        }
        return path.substring(path.indexOf("\\") + 1);
    }

    /**
     * 获取windows注册表
     *
     * @return Registry
     * */
    public static Registry getWindowsRegistry() {
        return REGISTRY;
    }

}