package com.lxgolovin.clouds.aws.s3;

import com.lxgolovin.clouds.aws.client.Client;
import com.lxgolovin.clouds.filesystem.DriveNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BucketTest {

    private final Bucket bucket = new Bucket(Client.getS3Client(TestsBase.region), TestsBase.existingBucketName);

    private Set<DriveNode> driveNodes;

    @BeforeEach
    void setUp() {
        driveNodes = bucket.readBucket(TestsBase.filter);
        assertNotNull(driveNodes);
    }

    @Test
    void nullChecked() {
        assertThrows(IllegalArgumentException.class, () -> new Bucket(null));
        assertThrows(IllegalArgumentException.class, () -> new Bucket(null, null));
        assertThrows(IllegalArgumentException.class, () -> new Bucket(Client.getS3Client(TestsBase.region), null));
    }

    @Test
    void readFilesFromBucket() {
        driveNodes = bucket.readBucket();
        assertNotNull(driveNodes);
        assertTrue(bucket.filesCount() >= 0);
        assertTrue(bucket.sizeTotalBytes() >= 0);

        driveNodes.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void readAndFilterFiles() {
        assertTrue(bucket.filesCount() >= 0);
        assertTrue(bucket.sizeTotalBytes() >= 0);

        driveNodes.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void saveFileLocally() throws IOException {
        // driveNodes.forEach(bucket::saveFileLocally);
        DriveNode driveNode = driveNodes.iterator().next();

        Files.deleteIfExists(Paths.get(driveNode.getPath()));
        assertTrue(bucket.saveFileLocally(driveNode));
        assertTrue(Files.exists(Paths.get(driveNode.getPath())));
        Files.deleteIfExists(Paths.get(driveNode.getPath()));

        String tempFile = "tempFile.txt";
        Files.deleteIfExists(Paths.get(tempFile));
        assertTrue(bucket.saveFileLocally(driveNodes.iterator().next(), tempFile));
        assertTrue(Files.exists(Paths.get(tempFile)));
        Files.deleteIfExists(Paths.get(tempFile));
    }

    @Test
    void getFile() {
        assertNotNull(bucket.getFile(driveNodes.iterator().next()));
    }
}