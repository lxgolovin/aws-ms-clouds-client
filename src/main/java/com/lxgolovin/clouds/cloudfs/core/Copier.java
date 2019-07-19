package com.lxgolovin.clouds.cloudfs.core;

import com.lxgolovin.clouds.aws.s3.Bucket;
import com.lxgolovin.clouds.msgraph.drive.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

public class Copier {
    private static Logger logger = LoggerFactory.getLogger(Copier.class);

    public static void copyAwsToMs(String bucketNameAws, String bucketNameMs) {
        if (isNull(bucketNameAws) || isNull(bucketNameMs)) {
            throw new IllegalArgumentException("Bucket name cannot be null");
        }

        Bucket bucket = new Bucket(bucketNameAws);
        logger.debug("Read bucket '{}':", bucketNameAws);
        logger.debug("Number of items: {}", bucket.filesCount());
        logger.debug("Bytes total: {}", bucket.sizeTotalBytes());

        bucket.readBucket(null)
                .stream()
                .filter(BucketItem::isFile)
                .forEach(bucketItem -> {
                    logger.debug("Path: '{}'; Size: {}; IsRegularFile: '{}'",
                            bucketItem.getPath(), bucketItem.getSize(), bucketItem.isFile());
                    File file = new File(bucketNameMs);
                    file.delete(bucketItem.getPath());
                    file.upload(bucket.readBucketItem(bucketItem), bucketItem.getPath());
                });
    }
}
