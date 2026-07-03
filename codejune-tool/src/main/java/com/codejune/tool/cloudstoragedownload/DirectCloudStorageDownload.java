package com.codejune.tool.cloudstoragedownload;

import com.codejune.tool.CloudStorageDownload;

public final class DirectCloudStorageDownload extends CloudStorageDownload {

    @Override
    public String getDirectUrl(String url) {
        return url;
    }

}