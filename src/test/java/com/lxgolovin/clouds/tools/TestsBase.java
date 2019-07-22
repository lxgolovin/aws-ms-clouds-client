package com.lxgolovin.clouds.tools;

import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestsBase {

    // s3 testing constants and tools
    public static final String folderName = "TEMP/NewFolder/NewFolder";

    public static final String fileName = "TEMP/NewFolder/NewFolder/apache-maven-3.6.1-bin.tar.gz";
//    static final String fileName = "TEMP/Instr2.mp4";

    public static final String bucket = "01XHM6HBUOKVNQM2MZERGLFEJ3FITM4CTP"; //Test upload folder
//    static final String bucket = "01XHM6HBV6Y2GOVW7725BZO354PWSELRRZ"; //Test upload folder

    public static final String filter = ".*gradle.*";

    public static final Region region = Region.EU_CENTRAL_1;

    public static final String existingBucketName = "company-saleselm-10001";

    public static final String bucketName = "bucket" + UUID.randomUUID();

    public static final String TEMP_FOLDER = "TEMP_FOLDER";

    public static void deleteDirectory(String path) throws IOException {
        assertNotNull(path);
        deleteDirectory(Paths.get(path));
    }

    public static void deleteDirectory(Path path) throws IOException {
        assertNotNull(path);
        if (path.toFile().exists()) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(f -> assertTrue(f.toFile().delete()));
        }

        assertFalse(Files.exists(path), "Directory is present");
    }

    public static ByteBuffer getRandomByteBuffer() {
        final int RANDOM_FILES_SIZE_IN_BYTES = 10_000;

        byte[] b = new byte[RANDOM_FILES_SIZE_IN_BYTES];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }
}