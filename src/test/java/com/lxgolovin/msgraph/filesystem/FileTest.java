package com.lxgolovin.msgraph.filesystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileTest {

    private final String fileName = TestsBase.fileName;

    private File file;

    @BeforeEach
    void setUp() {
        final String bucket = TestsBase.bucket;
        file = new File(bucket);
    }

    @Test
    void passNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new File(null, null));

        assertThrows(IllegalArgumentException.class, () -> file.upload(null, null));
        assertNull(file.getFileInfo(null));
        assertFalse(file.delete(null));
    }

    @Test
    void getFileInfoByPathAndDeleteFile() {
        try (InputStream uploadFileStream = Files.newInputStream(Paths.get(fileName))) {
            assertTrue(file.upload(uploadFileStream, fileName));
        } catch (IOException e) {
            throw new Error("File cannot be accessed: " + e.getLocalizedMessage());
        }

        assertNotNull(file.getFileInfo(fileName));
        assertNotNull(file.getFileInfo(fileName).file);
        assertTrue(file.delete(fileName));

        assertNotNull(file.getFileInfo(TestsBase.folderName).folder);
    }

    @Test
    void upload() {
        try (InputStream uploadFileStream = Files.newInputStream(Paths.get(fileName))) {
            assertTrue(file.upload(uploadFileStream, fileName));
        } catch (IOException e) {
            throw new Error("File cannot be accessed: " + e.getLocalizedMessage());
        }
        assertTrue(file.delete(fileName));
    }
}