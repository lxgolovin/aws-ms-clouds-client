package com.lxgolovin.clouds.aws.client;

import com.lxgolovin.clouds.config.Constants;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class Client {

    public static S3Client getS3Client() {
        // TODO: to replace with config file
        return getS3Client(Constants.DEFAULT_AWS_REGION);
    }

    public static S3Client getS3Client(Region region) {
        Region s3Region = (region == null) ? Constants.DEFAULT_AWS_REGION : region;
        return S3Client.builder().region(s3Region).build();
    }
}
