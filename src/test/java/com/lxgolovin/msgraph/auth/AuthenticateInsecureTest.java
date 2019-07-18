package com.lxgolovin.msgraph.auth;

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