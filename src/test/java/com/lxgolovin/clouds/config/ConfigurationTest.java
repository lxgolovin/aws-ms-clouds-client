package com.lxgolovin.clouds.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    private final Configuration configuration = new Configuration();

    @Test
    void nullTests() {
        Configuration config = new Configuration(null);
        assertNotNull(config.getAppId());
    }

    @Test
    void getConfiguration() {
        assertNotNull(configuration.getAppId());
        assertNotNull(configuration.getLogin());
        assertNotNull(configuration.getPassword());
        assertNotNull(configuration.getAppScopes());
    }
}