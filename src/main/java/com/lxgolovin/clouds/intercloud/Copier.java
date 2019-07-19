package com.lxgolovin.clouds.intercloud;

import com.lxgolovin.clouds.aws.s3.BucketAwsS3;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.msgraph.drive.BucketOneDrive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

public class Copier {

    private static Logger logger = LoggerFactory.getLogger(Copier.class);

    public static void copyAwsToMs(String bucketNameAws, String bucketNameMs) {
        if (isNull(bucketNameAws) || isNull(bucketNameMs)) {
            throw new IllegalArgumentException("BucketAwsS3 name cannot be null");
        }

        BucketAwsS3 bucketAwsS3 = new BucketAwsS3(bucketNameAws);
        logger.debug("Read bucketAwsS3 '{}':", bucketNameAws);
        logger.debug("Number of items: {}", bucketAwsS3.filesCount());
        logger.debug("Bytes total: {}", bucketAwsS3.sizeTotalBytes());

        bucketAwsS3.readBucket(null)
                .stream()
                .filter(BucketItem::isFile)
                .forEach(bucketItem -> {
                    logger.debug("Path: '{}'; Size: {}; IsRegularFile: '{}'",
                            bucketItem.getPath(), bucketItem.getSize(), bucketItem.isFile());
                    BucketOneDrive bucketOneDrive = new BucketOneDrive(bucketNameMs);
                    bucketOneDrive.delete(bucketItem.getPath());
                    bucketOneDrive.upload(bucketAwsS3.readBucketItem(bucketItem), bucketItem.getPath());
                });
    }
}
