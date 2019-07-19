package com.lxgolovin.clouds.msgraph.drive;

import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.msgraph.client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BucketMsTest {

    private final BucketMs bucketMs = new BucketMs(TestsBase.bucket);


    @BeforeEach
    void setUp() {
        Set<BucketItem> bucketItems = bucketMs.readBucket(TestsBase.filter);
        assertNotNull(bucketItems);
    }

    @Test
    void nullChecked() {
        assertThrows(IllegalArgumentException.class, () -> new BucketMs(null));
        assertThrows(IllegalArgumentException.class, () -> new BucketMs(null, null));
        assertThrows(IllegalArgumentException.class, () -> new BucketMs(Client.getMsGraphClient(), null));
    }
}