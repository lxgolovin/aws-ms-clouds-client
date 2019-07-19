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

    public Configuration() {
        this(Constants.DEFAULT_CONFIG_FILE);
    }

    public Configuration(Path configFilePath) {
        if (configFilePath == null) {
            configFilePath = Constants.DEFAULT_CONFIG_FILE;
        }

        final String APP_ID = "app.id";
        final String APP_SCOPES = "app.scopes";

        final String USER_LOGIN = "user.login";
        final String USER_PASSWORD = "user.password";

        final String PROXY_SERVER = "proxy.server";
        final String PROXY_PORT = "proxy.port";
        final String PROXY_USE = "proxy.use";

        readConfigFile(configFilePath);

        this.login = oAuthProperties.getProperty(USER_LOGIN);
        this.password = oAuthProperties.getProperty(USER_PASSWORD);

        this.appId = oAuthProperties.getProperty(APP_ID);
        this.appScopes = oAuthProperties.getProperty(APP_SCOPES).split(Constants.DEFAULT_SEPARATOR);

        this.proxyServer = oAuthProperties.getProperty(PROXY_SERVER);
        this.proxyPort = Integer.valueOf(oAuthProperties.getProperty(PROXY_PORT));
        this.isProxyUsed = (oAuthProperties.getProperty(PROXY_USE).equals("YES"));
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

    public String[] getAppScopes() {
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
}