package com.codejune.http;

import com.codejune.core.BaseException;
import com.codejune.core.Builder;
import com.codejune.core.util.FileUtil;
import com.codejune.core.util.IOUtil;
import com.codejune.core.util.ObjectUtil;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表单数据
 *
 * @author ZJ
 * */
public final class FormData implements Builder {

    private final List<FormDataItem> formDataItemList = new ArrayList<>();

    /**
     * 添加文本
     *
     * @param name name
     * @param data data
     * */
    public FormData addText(String name, String data) {
        FormDataItem formDataItem = new FormDataItem();
        formDataItem.setName(name);
        formDataItem.setContentType(ContentType.TEXT_PLAIN);
        formDataItem.setData(data);
        this.formDataItemList.add(formDataItem);
        return this;
    }

    /**
     * 添加文件
     *
     * @param name name
     * @param fileName fileName
     * @param inputStream inputStream
     * */
    public FormData addFile(String name, String fileName, InputStream inputStream) {
        FormDataItem formDataItem = new FormDataItem();
        formDataItem.setName(name);
        formDataItem.setFileName(fileName);
        formDataItem.setContentType(ContentType.DEFAULT_BINARY);
        formDataItem.setData(inputStream);
        this.formDataItemList.add(formDataItem);
        return this;
    }

    /**
     * 添加文件
     *
     * @param name name
     * @param file file
     * */
    public FormData addFile(String name, File file) {
        if (!FileUtil.isFile(file)) {
            throw new BaseException("not file");
        }
        return this.addFile(name, file.getName(), IOUtil.getInputStream(file));
    }

    /**
     * 获取FormDataItem
     *
     * @return FormDataItem
     * */
    public List<FormDataItem> getFormDataItem() {
        return this.formDataItemList;
    }

    @Override
    public void build(Object object) {
        switch (object) {
            case null -> {}
            case FormData formData -> {
                this.formDataItemList.clear();
                this.formDataItemList.addAll(formData.getFormDataItem());
            }
            case Map<?, ?> map -> {
                this.formDataItemList.clear();
                for (Object key : map.keySet()) {
                    String keyString = ObjectUtil.toString(key);
                    Object value = map.get(key);
                    if (value instanceof File file) {
                        this.addFile(keyString, file);
                    } else if (value instanceof InputStream inputStream) {
                        this.addFile(keyString, null, inputStream);
                    } else {
                        this.addText(keyString, ObjectUtil.toString(value));
                    }
                }
            }
            default -> throw new BaseException("not build formData");
        }
    }

    public static class FormDataItem {

        private String name;

        private String fileName;

        private ContentType contentType;

        private Object data;

        private FormDataItem() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public ContentType getContentType() {
            return contentType;
        }

        public void setContentType(ContentType contentType) {
            this.contentType = contentType;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

    }

}