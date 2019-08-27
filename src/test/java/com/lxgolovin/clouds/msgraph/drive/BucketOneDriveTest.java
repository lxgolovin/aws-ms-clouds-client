package com.lxgolovin.clouds.msgraph.drive;

import com.lxgolovin.clouds.tools.TestsBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class BucketOneDriveTest {

    private final String fileName = TestsBase.FILE_NAME;

    private BucketOneDrive bucketOneDrive;

    @BeforeEach
    void setUp() {
        bucketOneDrive = new BucketOneDrive(TestsBase.BUCKET);
    }

    @Test
    void passNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BucketOneDrive(null, null));

        assertThrows(IllegalArgumentException.class, () -> bucketOneDrive.upload(null, null));
        // assertNull(bucketOneDrive.getFileInfo(null));
        assertFalse(bucketOneDrive.delete(null));
    }

    @Test
    void getFileInfoByPathAndDeleteFile() throws IOException {
        try (InputStream uploadFileStream = Files.newInputStream(Paths.get(fileName))) {
            assertTrue(bucketOneDrive.upload(uploadFileStream, fileName));
        }

        assertNotNull(bucketOneDrive.getFileInfo(fileName));
        assertTrue(bucketOneDrive.getFileInfo(fileName).isFile());
        assertTrue(bucketOneDrive.delete(fileName));

        assertFalse(bucketOneDrive.getFileInfo(TestsBase.FOLDER_NAME).isFile());
    }

    @Test
    void upload() throws IOException {
        try (InputStream uploadFileStream = Files.newInputStream(Paths.get(fileName))) {
            assertTrue(bucketOneDrive.upload(uploadFileStream, fileName));
        }
        assertTrue(bucketOneDrive.delete(fileName));
    }
}