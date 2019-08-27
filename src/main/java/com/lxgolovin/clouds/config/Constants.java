package com.lxgolovin.clouds.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

    private Constants() {}

    static final Path DEFAULT_CONFIG_FILE = Paths.get("src/main/resources/oAuth.properties");

    public static final int DOWNLOAD_BUFFER_SIZE = 64 * 1024;

    public static final int ONE_DRIVE_MAX_CONTENT_SIZE = 4 * 1024 * 1024;

    public static final int PRINTABLE_CHUNK_SIZE = 40 * 1024 * 1024;

    public static final String DEFAULT_CLOUD_FS_FILTER = ".*";

    public static final String DEFAULT_SAVE_STATE_DIRECTORY = "processedFiles.state";

    public static final int HTTP_RESPONSE_NOT_FOUND = 404;

    public static final int AWS_TOKEN_EXPIRED = 400;

    public static final int ONE_DRIVE_TOKEN_EXPIRED = 401;

    static final String DEFAULT_AWS_REGION = "eu-central-1";

    public static final int RETRY_TIMES = 3;
}