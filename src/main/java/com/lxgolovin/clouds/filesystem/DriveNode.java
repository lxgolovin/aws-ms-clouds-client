package com.lxgolovin.clouds.filesystem;

import static java.util.Objects.isNull;

public final class DriveNode {

    private final String path;

    private final long size;

    private final boolean isFolder;

    private final String bucket;

    public DriveNode(String bucketName, String path, Long size, boolean isFolder) {
        if (isNull(path)) {
            throw new IllegalArgumentException();
        }

        this.path = path;
        this.size = (isNull(size)) ? 0 : size;
        this.isFolder = isFolder;
        this.bucket = bucketName;
    }

    public static String getPathToFile(String absPath) {
        return (absPath == null) ? "" : absPath.replaceAll("(.*)/.*", "$1");
    }

    public long getSize() {
        return size;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getPath() {
        return path;
    }
}
