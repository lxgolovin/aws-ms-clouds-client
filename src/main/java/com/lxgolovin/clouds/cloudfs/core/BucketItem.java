package com.lxgolovin.clouds.cloudfs.core;

import java.io.Serializable;

import static java.util.Objects.isNull;

public final class BucketItem implements Serializable {

    private final String path;

    private final long size;

    private final boolean isFile;

    public BucketItem(String path) {
        this(path, -1, true);
    }

    public BucketItem(String path, long size, boolean isFile) {
        if (isNull(path)) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        this.path = path;
        this.size = size;
        this.isFile = isFile;
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
}
