package com.lxgolovin.clouds.aws.s3;

import software.amazon.awssdk.regions.Region;

import java.util.UUID;

class TestsBase {

    static final Region region = Region.EU_CENTRAL_1;

    static final String existingBucketName = "company-saleselm-10001";

    static final String bucketName = "bucket" + UUID.randomUUID();

    static final String filter = ".*gradle.*";

    static final String TEMP_FOLDER = "TEMP_FOLDER";
}
