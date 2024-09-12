package com.codejune.http;

/**
 * ContentType
 *
 * @author ZJ
 * */
public enum ContentType {

    APPLICATION_JSON("application/json"),

    APPLICATION_XML("application/xml"),

    FORM_DATA("multipart/form-data"),

    TEXT_PLAIN("text/plain"),

    TEXT_HTML("text/html"),

    FORM_URLENCODED("application/x-www-form-urlencoded"),

    DEFAULT_BINARY("default/binary");

    private final String contentType;

    ContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

}