package com.lxgolovin.clouds.config;

import software.amazon.awssdk.regions.Region;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {


    static final Path DEFAULT_CONFIG_FILE = Paths.get("src/main/resources/oAuth.properties");

    static final String DEFAULT_SEPARATOR = ",";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final int DEFAULT_AWS_S3_CHUNK_SIZE = 8 * 1024 * 1024;

    public static final int ONE_DRIVE_MAX_CONTENT_SIZE = 4 * 1024 * 1024;

    public static final int MAXIMUM_AWS_S3_CHUNK_SIZE = Integer.MAX_VALUE - 8;

    public static final String DEFAULT_FILTER = ".*";

    public static final String DEFAULT_SAVE_STATE_DIRECTORY = "./TEMP/processedFiles.state";

    public static final int HTTP_RESPONSE_NOT_FOUND = 404;

//    public static Region DEFAULT_AWS_REGION = Region.EU_CENTRAL_1;
    private static Region DEFAULT_AWS_REGION = Region.US_EAST_1;
}