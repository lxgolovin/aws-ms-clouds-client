package com.lxgolovin.clouds.msgraph.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lxgolovin.clouds.config.Configuration;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class AuthenticateInsecure {

    private final String clientId;

    private final String username;

    private final String password;

    private String accessToken;

    private IGraphServiceClient graphClient;

    private final Configuration configuration = new Configuration();

    AuthenticateInsecure() {
        this.clientId = configuration.getAppId();
        this.username = configuration.getLogin();
        this.password = configuration.getPassword();

        GetAuthenticatedClient();
    }

    AuthenticateInsecure(String appId, String userName, String password) {
        this.clientId = appId;
        this.username = userName;
        this.password = password;

        GetAuthenticatedClient();
    }


    private void GetAuthenticatedClient() {
        try {
            accessToken = GetAccessToken().replace("\"", "");

            IAuthenticationProvider mAuthenticationProvider =
                    request -> request.addHeader("Authorization","Bearer " + accessToken);
            IClientConfig mClientConfig = DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationProvider);

            graphClient = GraphServiceClient.fromConfig(mClientConfig);
        } catch (Exception e) {
            throw new IllegalAccessError("Unable to create graph client: " + e.getLocalizedMessage());
        }
    }

    private String GetAccessToken() {
        // Don't use password grant in your apps. Only use for legacy solutions and automated testing.
        final String grantType = "password";
        final String resourceId = "https%3A%2F%2Fgraph.microsoft.com%2F";
        final String tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/token";

        try {
            URL url = new URL(tokenEndpoint);
            HttpURLConnection conn;
            if (configuration.isProxyUsed()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyServer(), configuration.getProxyPort()));
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            String line;
            StringBuilder jsonString = new StringBuilder();

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.connect();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            String payload = String.format("grant_type=%1$s&resource=%2$s&client_id=%3$s&username=%4$s&password=%5$s",
                    grantType,
                    resourceId,
                    clientId,
                    username,
                    password);

            outputStreamWriter.write(payload);
            outputStreamWriter.close();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                while((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            } catch (Exception e) {
                throw new IllegalAccessError("Unable to read authorization response: " + e.getLocalizedMessage());
            }
            conn.disconnect();

            JsonObject res = new GsonBuilder()
                    .create()
                    .fromJson(jsonString.toString(), JsonObject.class);

            return res
                    .get("access_token")
                    .toString()
                    .replaceAll("\"", "");

        } catch (Exception e) {
            throw new IllegalAccessError("Unable to read authorization response: " + e.getLocalizedMessage());
        }
    }

    IGraphServiceClient getGraphClient() {
        return graphClient;
    }

    String getAccessToken() {
        return accessToken;
    }
}
