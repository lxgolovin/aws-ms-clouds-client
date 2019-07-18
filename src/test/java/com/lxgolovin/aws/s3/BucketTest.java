package com.lxgolovin.aws.s3;

import com.lxgolovin.aws.client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BucketTest {

    private final Bucket bucket = new Bucket(Client.getS3Client(TestBase.region), TestBase.existingBucketName);

    private Set<FileNode> fileNodes;

    @BeforeEach
    void setUp() {
        fileNodes = bucket.readBucket(TestBase.filter);
        assertNotNull(fileNodes);
    }

    @Test
    void nullChecked() {
        assertThrows(IllegalArgumentException.class, () -> new Bucket(null));
        assertThrows(IllegalArgumentException.class, () -> new Bucket(null, null));
        assertThrows(IllegalArgumentException.class, () -> new Bucket(Client.getS3Client(TestBase.region), null));
    }

    @Test
    void readFilesFromBucket() {
        fileNodes = bucket.readBucket();
        assertNotNull(fileNodes);
        assertTrue(bucket.filesCount() >= 0);
        assertTrue(bucket.sizeTotalBytes() >= 0);

        fileNodes.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void readAndFilterFiles() {
        assertTrue(bucket.filesCount() >= 0);
        assertTrue(bucket.sizeTotalBytes() >= 0);

        fileNodes.forEach(f -> assertTrue(f.getSize() >= 0));
    }

    @Test
    void saveFileLocally() throws IOException {
        // fileNodes.forEach(bucket::saveFileLocally);
        FileNode fileNode = fileNodes.iterator().next();

        Files.deleteIfExists(Paths.get(fileNode.getPath()));
        assertTrue(bucket.saveFileLocally(fileNode));
        assertTrue(Files.exists(Paths.get(fileNode.getPath())));
        Files.deleteIfExists(Paths.get(fileNode.getPath()));

        String tempFile = "tempFile.txt";
        Files.deleteIfExists(Paths.get(tempFile));
        assertTrue(bucket.saveFileLocally(fileNodes.iterator().next(), tempFile));
        assertTrue(Files.exists(Paths.get(tempFile)));
        Files.deleteIfExists(Paths.get(tempFile));
    }

    @Test
    void getFile() {
        assertNotNull(bucket.getFile(fileNodes.iterator().next()));
    }
}