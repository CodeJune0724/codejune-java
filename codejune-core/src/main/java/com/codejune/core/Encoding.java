package com.codejune.core;

import java.nio.charset.Charset;

/**
 * 编码
 *
 * @author ZJ
 * */
public final class Encoding {

    public static final Charset NATIVE = Charset.forName(System.getProperty("native.encoding"));

    public static final Charset FILE = Charset.defaultCharset();

    public static final Charset STDIN = Charset.forName(System.getProperty("stdin.encoding"));

    public static final Charset STDOUT = Charset.forName(System.getProperty("stdout.encoding"));

    public static final Charset STDERR = Charset.forName(System.getProperty("stderr.encoding"));

}