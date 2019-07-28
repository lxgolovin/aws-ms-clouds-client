package com.lxgolovin.clouds.cloudfs.core;

import static java.util.Objects.isNull;

public final class BucketItem {

    private final String path;

    private final int size;

    private final boolean isFile;

    public BucketItem(String path) {
        this(path, -1, true);
    }

    public BucketItem(String path, long size, boolean isFile) {
        this(path, (int) size, isFile);

    }

    private BucketItem(String path, int size, boolean isFile) {
        if (isNull(path)) {
            throw new IllegalArgumentException();
        }

        this.path = path;
        this.size = size;
        this.isFile = isFile;
    }

    public static String getParentFolder(String pathToFile) {
        return (pathToFile == null) ? "" : pathToFile.replaceAll("(.*)/.*", "$1");
    }

    public int getSize() {
        return size;
    }

    public boolean isFile() {
        return isFile;
    }

    public String getPath() {
        return path;
    }
}
