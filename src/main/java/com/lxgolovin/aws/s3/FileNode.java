package com.lxgolovin.aws.s3;

import static java.util.Objects.isNull;

public final class FileNode {

    private final String path;

    private final long size;

    private final boolean isFolder;


    FileNode(String path, Long size, boolean isFolder) {
        if (isNull(path)) {
            throw new IllegalArgumentException();
        }

        this.path = path;
        this.size = (isNull(size)) ? 0 : size;
        this.isFolder = isFolder;
    }

    static String getPathToFile(String absPath) {
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
