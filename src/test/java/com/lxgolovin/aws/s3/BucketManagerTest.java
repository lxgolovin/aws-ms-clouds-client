package com.lxgolovin.aws.s3;

import com.lxgolovin.aws.client.Client;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class BucketManagerTest {

    private static final S3Client s3 = Client.getS3Client(TestBase.region);

    private final String bucket = TestBase.bucketName;

    private BucketManager bucketManager = new BucketManager(s3);

    @Test
    void createListDeleteEmptyBucket() {
        assertTrue(bucketManager.createBucket(bucket));
        assertTrue(
                bucketManager.listBuckets()
                        .stream()
                        .map(Bucket::name)
                        .anyMatch(x -> x.equals(bucket)));
        assertNotNull(bucketManager.getLocation(bucket));
        assertTrue(bucketManager.deleteBucket(bucket));
    }

    @Test
    void createNonEmptyBucket() {
        assertTrue(bucketManager.createBucket(bucket));
        putFakeObjects();
        assertFalse(bucketManager.deleteBucket(bucket));
        // TODO: test to delete not empty bucket
    }

    private void putFakeObjects() {
        final int RANDOM_FILES_NUMBER = 5;

        IntStream.rangeClosed(0, RANDOM_FILES_NUMBER)
                .forEach(i -> s3.putObject(
                        PutObjectRequest
                                .builder()
                                .bucket(bucket)
                                .key("folder/file_" + i)
                                .build(),
                        RequestBody.fromByteBuffer(getRandomByteBuffer())));
    }

    private ByteBuffer getRandomByteBuffer() {
        final int RANDOM_FILES_SIZE_IN_BYTES = 10_000;

        byte[] b = new byte[RANDOM_FILES_SIZE_IN_BYTES];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }
}