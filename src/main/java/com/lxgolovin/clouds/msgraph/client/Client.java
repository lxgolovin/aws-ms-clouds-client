package com.lxgolovin.clouds.msgraph.client;

import com.microsoft.graph.models.extensions.IGraphServiceClient;

public final class Client {

    private Client() {}

    public static IGraphServiceClient getMsGraphClient() {
        return new AuthenticateInsecure().getGraphClient();
    }
}
