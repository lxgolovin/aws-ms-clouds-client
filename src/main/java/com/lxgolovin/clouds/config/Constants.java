package com.lxgolovin.clouds.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

    static final Path DEFAULT_CONFIG_FILE = Paths.get("src/main/resources/oAuth.properties");

    static final String DEFAULT_SEPARATOR = ",";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final int DEFAULT_AWS_S3_CHUNK_SIZE = 8 * 1024 * 1024;

    public static final int ONE_DRIVE_MAX_CONTENT_SIZE = 8 * 1024 * 1024;

    public static final int MAXIMUM_AWS_S3_CHUNK_SIZE = Integer.MAX_VALUE - 8;
}
