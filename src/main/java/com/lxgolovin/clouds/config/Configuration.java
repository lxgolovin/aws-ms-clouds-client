package com.lxgolovin.clouds.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Configuration {

    private final Properties oAuthProperties = new Properties();

    private final String login;

    private final String password;

    private final String appId;

    private String[] appScopes;

    private final String proxyServer;

    private final int proxyPort;

    private final boolean isProxyUsed;

    private final String awsRegion;

    private final String awsAccessKeyId;

    private final String awsSecretAccessKey;

    private final String awsSessionToken;

    private final String awsBucketStatePath;

    public Configuration() {
        this(Constants.DEFAULT_CONFIG_FILE);
    }

    Configuration(Path path) {
        Path configFilePath = (path == null) ? Constants.DEFAULT_CONFIG_FILE : path;

        final String APP_ID = "app.id";
        final String APP_SCOPES = "app.scopes";

        final String USER_LOGIN = "user.login";
        final String USER_PASSWORD = "user.password";

        final String PROXY_SERVER = "proxy.server";
        final String PROXY_PORT = "proxy.port";
        final String PROXY_USE = "proxy.use";

        final String AWS_REGION = "aws.region";
        final String AWS_ACCESS_KEY_ID = "aws.accessKeyId";
        final String AWS_SECRET_ACCESS_KEY = "aws.secretAccessKey";
        final String AWS_SESSION_TOKEN = "aws.sessionToken";
        final String AWS_BUCKET_STATE_PATH = "aws.bucketStatePath";

        readConfigFile(configFilePath);

        this.login = oAuthProperties.getProperty(USER_LOGIN);
        this.password = oAuthProperties.getProperty(USER_PASSWORD);

        this.appId = oAuthProperties.getProperty(APP_ID);
        this.appScopes = oAuthProperties.getProperty(APP_SCOPES).split(Constants.DEFAULT_CONFIG_FILE_SEPARATOR);

        this.proxyServer = oAuthProperties.getProperty(PROXY_SERVER);
        this.proxyPort = Integer.valueOf(oAuthProperties.getProperty(PROXY_PORT));
        this.isProxyUsed = "YES".equals(oAuthProperties.getProperty(PROXY_USE));

        String awsRegion = oAuthProperties.getProperty(AWS_REGION);
        if (awsRegion == null) {
            awsRegion = Constants.DEFAULT_AWS_REGION;
        }
        this.awsRegion = awsRegion;

        this.awsAccessKeyId = oAuthProperties.getProperty(AWS_ACCESS_KEY_ID);
        this.awsSecretAccessKey = oAuthProperties.getProperty(AWS_SECRET_ACCESS_KEY);
        this.awsSessionToken = oAuthProperties.getProperty(AWS_SESSION_TOKEN);
        this.awsBucketStatePath = oAuthProperties.getProperty(AWS_BUCKET_STATE_PATH);

        setProxy();
        setAwsToken();
        checkProperties();
    }

    private void checkProperties() {
        if ((awsBucketStatePath == null) || awsBucketStatePath.isEmpty()) {
            throw new IllegalArgumentException("Bucket save path should be defined in config file, field 'aws.bucketStatePath'");
        }
    }

    private void setAwsToken() {
        if (this.awsAccessKeyId != null) {
            System.setProperty("aws.accessKeyId", this.awsAccessKeyId);
        }

        if (this.awsSecretAccessKey != null) {
            System.setProperty("aws.secretAccessKey", this.awsSecretAccessKey);
        }

        if (this.awsSessionToken != null){
            System.setProperty("aws.sessionToken", this.awsSessionToken);
        }
    }

    private void setProxy() {
        if (this.isProxyUsed) {
            System.setProperty("http.proxyHost", this.proxyServer);
            System.setProperty("https.proxyHost", this.proxyServer);
            System.setProperty("http.proxyPort", String.valueOf(this.proxyPort));
            System.setProperty("https.proxyPort", String.valueOf(this.proxyPort));
        }
    }

    private void readConfigFile(Path configFilePath) {
        try (InputStream inputStream = Files.newInputStream(configFilePath)) {
            oAuthProperties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalAccessError("Unable to read configuration file: " + e.getLocalizedMessage());
        }
    }

    public String getLogin() {
        return login;
    }

    public String getAppId() {
        return appId;
    }

    public String getPassword() {
        return password;
    }

    String[] getAppScopes() {
        return appScopes;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public boolean isProxyUsed() {
        return isProxyUsed;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getAwsBucketStatePath() {
        return awsBucketStatePath;
    }
}