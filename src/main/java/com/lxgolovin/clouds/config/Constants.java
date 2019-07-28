package com.lxgolovin.clouds.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

    static final Path DEFAULT_CONFIG_FILE = Paths.get("src/main/resources/oAuth.properties");

    static final String DEFAULT_SEPARATOR = ",";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    public static final int ONE_DRIVE_MAX_CONTENT_SIZE = 4 * 1024 * 1024;

    public static final int MAXIMUM_AWS_S3_CHUNK_SIZE = 40 * 1024 * 1024;

    public static final String DEFAULT_FILTER = ".*";

    public static final String DEFAULT_SAVE_STATE_DIRECTORY = "processedFiles.state";

    public static final int HTTP_RESPONSE_NOT_FOUND = 404;

    static final String DEFAULT_AWS_REGION = "eu-central-1";

    public static final String REGEX_IS_FOLDER = ".*/$";

    public static int RETRY_TIMES = 3;
}