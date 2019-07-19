package com.lxgolovin.clouds.cloudfs.core;

import static java.util.Objects.isNull;

public final class BucketItem {

    private final String path;

    private final long size;

    private final boolean isFile;

    private final String parentBucket;

    public BucketItem(String parentBucket, String path, long size, boolean isFile) {
        if (isNull(path)) {
            throw new IllegalArgumentException();
        }

        this.path = path;
        this.size = (isNull(size)) ? 0 : size;
        this.isFile = isFile;
        this.parentBucket = parentBucket;
    }

    public static String getParentFolder(String pathToFile) {
        return (pathToFile == null) ? "" : pathToFile.replaceAll("(.*)/.*", "$1");
    }

    public long getSize() {
        return size;
    }

    public boolean isFile() {
        return isFile;
    }

    public String getPath() {
        return path;
    }

    public String getParentBucket() {
        return parentBucket;
    }
}
