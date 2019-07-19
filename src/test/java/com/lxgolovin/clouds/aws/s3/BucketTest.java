package com.lxgolovin.clouds.aws.s3;

import com.lxgolovin.clouds.aws.client.Client;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BucketTest {

    private final Bucket bucket = new Bucket(Client.getS3Client(TestsBase.region), TestsBase.existingBucketName);

    private Set<BucketItem> bucketItems;

    @BeforeEach
    void setUp() {
        bucketItems = bucket.readBucket(TestsBase.filter);
        assertNotNull(bucketItems);
    }

    @Test
    void nullChecked() {
        assertThrows(IllegalArgumentException.class, () -> new Bucket(null));
        assertThrows(IllegalArgumentException.class, () -> new Bucket(null, null));
        assertThrows(IllegalArgumentException.class, () -> new Bucket(Client.getS3Client(TestsBase.region), null));
    }

    @Test
    void readFilesFromBucket() {
        bucketItems = bucket.readBucket();
        assertNotNull(bucketItems);
        assertTrue(bucket.filesCount() >= 0);
        assertTrue(bucket.sizeTotalBytes() >= 0);

        bucketItems.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void readAndFilterFiles() {
        assertTrue(bucket.filesCount() >= 0);
        assertTrue(bucket.sizeTotalBytes() >= 0);

        bucketItems.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void saveFileLocally() throws IOException {
        BucketItem bucketItem = bucketItems.iterator().next();

        Files.deleteIfExists(Paths.get(bucketItem.getPath()));
        assertTrue(bucket.saveBucketItem(bucketItem));
        assertTrue(Files.exists(Paths.get(bucketItem.getPath())));
        Files.deleteIfExists(Paths.get(bucketItem.getPath()));

        Files.deleteIfExists(Paths.get(TestsBase.TEMP_FOLDER));
        assertTrue(bucket.saveBucketItem(bucketItems.iterator().next(), TestsBase.TEMP_FOLDER));
        assertTrue(Files.exists(Paths.get(TestsBase.TEMP_FOLDER)));
        // TODO: recursive delete Files.deleteIfExists(Paths.get(TestsBase.TEMP_FOLDER));
    }

    @Test
    void getFile() {
        assertNotNull(bucket.readBucketItem(bucketItems.iterator().next()));
    }
}