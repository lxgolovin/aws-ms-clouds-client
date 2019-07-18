package com.lxgolovin.aws.s3;

import com.lxgolovin.aws.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.List;

public class BucketManager {

    private final S3Client s3;

    private Logger logger = LoggerFactory.getLogger(BucketManager.class);

    public BucketManager() {
        this(Client.getS3Client());
    }

    public BucketManager(S3Client s3Client) {
        this.s3 = (s3Client == null) ? Client.getS3Client() : s3Client;
    }

    public List<Bucket> listBuckets() {
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);

        return listBucketsResponse.buckets();
    }

    boolean createBucket(String bucketName) {
        boolean isBucketCreated = false;
        CreateBucketRequest createBucketRequest = CreateBucketRequest
                .builder()
                .bucket(bucketName)
                .createBucketConfiguration(
                        CreateBucketConfiguration
                                .builder()
                                .build())
                .build();

        try {
            s3.createBucket(createBucketRequest);
            logger.info("Bucket {} created", bucketName);
            isBucketCreated = true;
        } catch (AwsServiceException | SdkClientException e) {
            logger.error("Unable to create bucket {}: {}", bucketName, e.getLocalizedMessage());
        }

        return isBucketCreated;
    }

    public String getLocation(String bucket) {
        try {
            return s3.getBucketLocation(GetBucketLocationRequest
                    .builder()
                    .bucket(bucket)
                    .build()
            ).locationConstraintAsString();
        } catch (S3Exception e) {
            logger.error("Cannot get location: {}", e.getLocalizedMessage());
            return null;
        }
    }

    boolean deleteBucket(String bucketName) {
        // TODO: need to implement check if the bucket is empty or not and then delete in different ways
        return deleteEmptyBucket(bucketName);
    }

    private boolean deleteEmptyBucket(String bucketName) {
        boolean isBucketDeleted = false;

        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
        try {
            s3.deleteBucket(deleteBucketRequest);
            isBucketDeleted = true;
        } catch (AwsServiceException | SdkClientException e) {
            logger.error("Unable to delete bucket {}: {}", bucketName, e.getLocalizedMessage());
        }
        return isBucketDeleted;
    }
}
