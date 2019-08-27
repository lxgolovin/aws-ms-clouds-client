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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestsBase {

    private TestsBase() {}

    // s3 testing constants and tools
    public static final String FOLDER_NAME = "TEMP/NewFolder/NewFolder";

    public static final String FILE_NAME = "TEMP/apache-maven-3.6.1-bin.tar.gz";

    public static final String BUCKET = "01XHM6HBUOKVNQM2MZERGLFEJ3FITM4CTP"; //Test upload folder

    public static final Region REGION = Region.EU_CENTRAL_1;

    public static final String EXISTING_BUCKET_NAME = "company-saleselm-10001";

    public static final String BUCKET_NAME = "BUCKET" + UUID.randomUUID();

    public static final String TEMP_FOLDER = "TEMP_FOLDER";

    public static void deleteDirectory(String path) throws IOException {
        assertNotNull(path);
        deleteDirectory(Paths.get(path));
    }

    private static void deleteDirectory(Path path) throws IOException {
        assertNotNull(path);
        if (path.toFile().exists()) {
            try (Stream<Path> dirTree = Files.walk(path)) {
                dirTree.sorted(Comparator.reverseOrder())
                        .forEach(f -> assertTrue(f.toFile().delete()));
            }
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