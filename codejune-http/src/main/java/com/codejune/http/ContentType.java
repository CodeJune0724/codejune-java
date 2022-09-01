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

    ROW("text/plain");

    private final String contentType;

    ContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

}