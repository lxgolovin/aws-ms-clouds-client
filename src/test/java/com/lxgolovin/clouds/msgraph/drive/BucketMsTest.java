package com.lxgolovin.clouds.msgraph.drive;

import com.lxgolovin.clouds.filesystem.DriveNode;
import com.lxgolovin.clouds.msgraph.auth.AuthenticateInsecure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BucketMsTest {

    private final BucketMs bucketMs = new BucketMs(TestsBase.bucket);


    @BeforeEach
    void setUp() {
        Set<DriveNode> driveNodes = bucketMs.readBucket(TestsBase.filter);
        assertNotNull(driveNodes);
    }

    @Test
    void nullChecked() {
        assertThrows(IllegalArgumentException.class, () -> new BucketMs(null));
        assertThrows(IllegalArgumentException.class, () -> new BucketMs(null, null));
        assertThrows(IllegalArgumentException.class, () -> new BucketMs(AuthenticateInsecure.initGraphClient(), null));
    }
}