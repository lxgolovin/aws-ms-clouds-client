package com.lxgolovin.clouds.aws.s3;

import com.lxgolovin.clouds.aws.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Optional;

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
        // TODO: need to implement other interface, not using BucketAwsS3 from sdk
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);

        return listBucketsResponse.buckets();
    }

    boolean createBucket(String bucketName) {
        if (bucketName == null)
            return false;
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
            logger.info("BucketAwsS3 {} created", bucketName);
            isBucketCreated = true;
        } catch (AwsServiceException e) {
            logger.error("Unable to create bucket {}: message {}, code: {}", bucketName, e.toBuilder().message(), e.toBuilder().statusCode());
        } catch (SdkClientException e) {
            logger.error("Unable to create bucket {}: {}", bucketName, e.getLocalizedMessage());
        }

        return isBucketCreated;
    }

    public String getLocation(String bucketName) {
        Optional<String> location = Optional.empty();
        try {
            location = Optional.ofNullable(s3.getBucketLocation(GetBucketLocationRequest
                    .builder()
                    .bucket(bucketName)
                    .build()
            ).locationConstraintAsString());
        } catch (AwsServiceException e) {
            logger.error("Cannot get location: message {}, code: {}", e.toBuilder().message(), e.toBuilder().statusCode());
        } catch (SdkClientException e) {
            logger.error("Not possible to get location for the bucket {}: {}", bucketName, e.getLocalizedMessage());
        }

        return location.orElse("");
    }

    boolean deleteBucket(String bucketName) {
        // TODO: need to implement check if the bucket is empty or not and then delete in different ways
        return (bucketName != null) && deleteEmptyBucket(bucketName);
    }

    private boolean deleteEmptyBucket(String bucketName) {
        boolean isBucketDeleted = false;

        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
        try {
            s3.deleteBucket(deleteBucketRequest);
            isBucketDeleted = true;
        } catch (AwsServiceException e) {
            logger.error("Unable to delete the bucket {}: message {}, code: {}", bucketName, e.toBuilder().message(), e.toBuilder().statusCode());
        } catch (SdkClientException e) {
            logger.error("Not possible to get location for the bucket {}: {}", bucketName, e.getLocalizedMessage());
        }
        return isBucketDeleted;
    }
}
