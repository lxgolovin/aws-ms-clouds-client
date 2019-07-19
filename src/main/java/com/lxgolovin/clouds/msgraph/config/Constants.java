package com.lxgolovin.clouds.msgraph.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

    static final Path DEFAULT_CONFIG_FILE = Paths.get("src/main/resources/oAuth.properties");

    static final String DEFAULT_SEPARATOR = ",";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static boolean USE_PROXY = false;
    public static String DEFAULT_PROXY_SERVER = "proxy-qa.aws.wiley.com";
    public static int DEFAULT_PROXY_PORT = 8080;

    public static final int DEFAULT_CHUNK_SIZE = 8 * 1024 * 1024;
    public static final int MAXIMUM_CHUNK_SIZE = Integer.MAX_VALUE - 8;
}
