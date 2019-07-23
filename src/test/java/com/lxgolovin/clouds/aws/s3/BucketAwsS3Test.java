package com.lxgolovin.clouds.aws.s3;

import com.lxgolovin.clouds.aws.client.Client;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.tools.TestsBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static com.lxgolovin.clouds.tools.TestsBase.deleteDirectory;
import static org.junit.jupiter.api.Assertions.*;

class BucketAwsS3Test {

    private final BucketAwsS3 bucketAwsS3 = new BucketAwsS3(Client.getS3Client(TestsBase.region), TestsBase.existingBucketName, null);

    private Set<BucketItem> bucketItems;

    @BeforeEach
    void setUp() {
        bucketItems = bucketAwsS3.readBucket();
        assertNotNull(bucketItems);
    }

    @Test
    void nullChecked() {
        assertThrows(IllegalArgumentException.class, () -> new BucketAwsS3(null, null));
        assertThrows(IllegalArgumentException.class, () -> new BucketAwsS3(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new BucketAwsS3(Client.getS3Client(TestsBase.region), null, null));
    }

    @Test
    void readFilesFromBucket() {
        bucketItems = bucketAwsS3.readBucket();
        assertNotNull(bucketItems);
        assertTrue(bucketAwsS3.filesCount() >= 0);
        assertTrue(bucketAwsS3.sizeTotalBytes() >= 0);

        bucketItems.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void readAndFilterFiles() {
        assertTrue(bucketAwsS3.filesCount() >= 0);
        assertTrue(bucketAwsS3.sizeTotalBytes() >= 0);

        bucketItems.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void saveFileLocally() throws IOException {
        BucketItem bucketItem = bucketItems.iterator().next();

        Files.deleteIfExists(Paths.get(bucketItem.getPath()));
        assertTrue(bucketAwsS3.saveBucketItem(bucketItem));
        // delete file
        assertTrue(Files.exists(Paths.get(bucketItem.getPath())));
        Files.deleteIfExists(Paths.get(bucketItem.getPath()));
        if (!bucketItem.isFile()) {
            String folder = BucketItem.getParentFolder(bucketItem.getPath());
            deleteDirectory(folder);
        }

        deleteDirectory(TestsBase.TEMP_FOLDER);
        assertTrue(bucketAwsS3.saveBucketItem(bucketItems.iterator().next(), TestsBase.TEMP_FOLDER));
        deleteDirectory(TestsBase.TEMP_FOLDER);
        assertFalse(Files.exists(Paths.get(TestsBase.TEMP_FOLDER)));
    }

    @Test
    void getFile() {
        assertTrue(bucketItems.stream().anyMatch(BucketItem::isFile));
    }


}