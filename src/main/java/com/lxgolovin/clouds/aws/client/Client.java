package com.lxgolovin.clouds.aws.client;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class Client {

//    private static Region DEFAULT_AWS_REGION = Region.US_EAST_1;
    private static Region DEFAULT_AWS_REGION = Region.EU_CENTRAL_1;

    public static S3Client getS3Client() {
        return getS3Client(DEFAULT_AWS_REGION);
    }

    public static S3Client getS3Client(Region region) {
        Region s3Region = (region == null) ? DEFAULT_AWS_REGION : region;
        return S3Client.builder().region(s3Region).build();
    }
}
