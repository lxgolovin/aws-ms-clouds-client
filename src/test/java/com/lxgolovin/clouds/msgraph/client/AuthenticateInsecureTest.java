package com.lxgolovin.clouds.msgraph.client;

import com.lxgolovin.clouds.msgraph.client.AuthenticateInsecure;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticateInsecureTest {

    @Test
    void nullConstructor() {
        assertThrows(Error.class, () -> new AuthenticateInsecure(null, null, null));
    }

    @Test
    void getGraphClient1() {
        AuthenticateInsecure authenticateInsecure = new AuthenticateInsecure();
        assertNotNull(authenticateInsecure.getGraphClient());
    }

    @Test
    void getAccessToken1() {
        AuthenticateInsecure authenticateInsecure = new AuthenticateInsecure();
        assertNotNull(authenticateInsecure.getAccessToken());
    }
}