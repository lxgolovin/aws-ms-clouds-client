package com.lxgolovin.clouds.tasks;

import com.lxgolovin.clouds.aws.s3.BucketAwsS3;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.msgraph.drive.BucketOneDrive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class Copier {

    private static Logger logger = LoggerFactory.getLogger(Copier.class);

    private Map<String, Long> processedFiles;


    public void copyAwsToMs(String bucketNameAws, String bucketNameMs, String filter) {
        if (isNull(bucketNameAws) || isNull(bucketNameMs)) {
            throw new IllegalArgumentException("BucketAwsS3 name cannot be null");
        }
        final String DEFAULT_SAVE_STATE_DIRECTORY = "./TEMP/processedFiles.state";

        final String sopyFilter = (filter == null) ? ".*" : filter;


        processedFiles = readState(Paths.get(DEFAULT_SAVE_STATE_DIRECTORY));
        BucketAwsS3 bucketAwsS3 = new BucketAwsS3(bucketNameAws);
        logger.debug("Read bucketAwsS3 '{}':", bucketNameAws);
        logger.debug("Number of items: {}", bucketAwsS3.filesCount());
        logger.debug("Bytes total: {}", bucketAwsS3.sizeTotalBytes());


        bucketAwsS3.readBucket(filter)
                .stream()
                .filter(BucketItem::isFile)
                .filter(b -> {
                    long fileSizeInMs = new BucketOneDrive(bucketNameMs)
                            .getFileInfo(b.getPath())
                            .getSize();
                    String file = b.getPath();
                    return processedFiles
                            .entrySet()
                            .stream()
                            .filter(e -> e.getKey().equals(file))
                            .filter(e -> e.getValue() == b.getSize())
                            .anyMatch(e -> e.getValue() != fileSizeInMs);
                })
                .forEach(b -> {
                    processedFiles.remove(b.getPath());
                    logger.debug("File '{}' processed, but not equal to original. Should be resend", b.getPath());
                });


        bucketAwsS3.readBucket(filter)
                .stream()
                .filter(BucketItem::isFile)
                .filter(bucketItem -> !processedFiles.containsKey(bucketItem.getPath()))
                .forEach(bucketItem -> {
                    logger.debug("Path: '{}'; Size: {}; IsRegularFile: Yes",
                            bucketItem.getPath(), bucketItem.getSize());
                    BucketOneDrive bucketOneDrive = new BucketOneDrive(bucketNameMs);
                    bucketOneDrive.delete(bucketItem.getPath());
                    boolean isUploaded = bucketOneDrive.upload(bucketAwsS3.readBucketItem(bucketItem), bucketItem.getPath());
                    if (isUploaded) {
                        processedFiles.put(bucketItem.getPath(), bucketItem.getSize());
                    }
                });

        // TODO: remove this line after debugging
        processedFiles.forEach((k, v) -> logger.debug("File '{}', size '{}' processed", k, v));
        saveState(processedFiles, Paths.get(DEFAULT_SAVE_STATE_DIRECTORY));
    }

    private void saveState(Map<?, ?> map, Path path) {
        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(map);
            objectOutputStream.flush();
        } catch (IOException e) {
            logger.error("Cannot save state of copying {}: {}", path.toUri(), e.getLocalizedMessage());
            throw new IllegalAccessError();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> readState(Path path) {
        Map<String , Long> map;

        try (InputStream inputStream = Files.newInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            map = (Map<String, Long>) objectInputStream.readObject();
        } catch (Exception e) {
            logger.error("Cannot read state file {}: {}", path.toUri(), e.getLocalizedMessage());
            map = new HashMap<>();
        }

        return map;
    }
}
