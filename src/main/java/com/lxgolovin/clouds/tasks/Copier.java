package com.lxgolovin.clouds.tasks;

import com.lxgolovin.clouds.aws.s3.BucketAwsS3;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.config.Constants;
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

    private final Logger logger = LoggerFactory.getLogger(Copier.class);

    private Map<String, Long> processedFiles;
    private Map<String, Long> unprocessedFiles = new HashMap<>();
    private final String bucketNameAws;
    private final String bucketNameMs;
    private final BucketAwsS3 bucketAwsS3;
    private final BucketOneDrive bucketOneDrive;

    public Copier(String bucketNameAws, String bucketNameMs, String prefix) {
        // TODO: to be replaced just with source and target
        this.bucketNameAws = bucketNameAws;
        this.bucketNameMs = bucketNameMs;
        this.bucketAwsS3 = new BucketAwsS3(bucketNameAws, prefix);
        this.bucketOneDrive = new BucketOneDrive(bucketNameMs);

    }


    public void copyAwsToMs(String filter) {
        if (isNull(bucketNameAws) || isNull(bucketNameMs)) {
            throw new IllegalArgumentException("BucketAwsS3 name cannot be null");
        }

        String copyFilter = (filter == null) ? Constants.DEFAULT_CLOUD_FS_FILTER : filter;


        processedFiles = readState(Paths.get(bucketNameAws + "_" + Constants.DEFAULT_SAVE_STATE_DIRECTORY));
        logger.debug("Read bucketAwsS3 '{}':", bucketNameAws);
        logger.debug("Number of items: {}", bucketAwsS3.itemsCount());
        logger.debug("Number of files: {}", bucketAwsS3.filesCount());
        logger.debug("Total: {} bytes; {} MB", bucketAwsS3.sizeTotalBytes(), bucketAwsS3.sizeTotalBytes()/(1024*1024));

        bucketAwsS3.readBucket()
                .stream()
                .filter(BucketItem::isFile)
                .filter(bucketItem -> bucketItem.getPath().matches(copyFilter))
                .filter(this::needToProcessBucketItem)
                .forEach(bucketItem -> {
                    String filePath = bucketItem.getPath();
                    long fileSize = bucketItem.getSize();

                    logger.debug("Processing: '{}'; Size: {}", filePath, fileSize);
                    boolean isUploaded = bucketOneDrive.upload(bucketAwsS3.readBucketItem(bucketItem), filePath);
                    long fileSizeInMs = bucketOneDrive.getFileInfo(filePath).getSize();
                    if (isUploaded || (fileSizeInMs == fileSize)) {
                        logger.debug("File uploaded: '{}'; Size: {}", filePath, fileSize);
                        processedFiles.put(filePath, fileSize);
                        unprocessedFiles.remove(filePath);
                    } else {
                        logger.error("File NOT uploaded: '{}'; Size: {}", filePath, fileSize);
                        processedFiles.remove(filePath);
                        unprocessedFiles.put(filePath, fileSize);
                    }
                });

        // TODO: remove this line after debugging
        processedFiles.forEach((k, v) -> logger.debug("File '{}', size '{}' processed", k, v));
        unprocessedFiles.forEach((k, v) -> logger.debug("File '{}', size '{}' NOT processed", k, v));
        saveState(processedFiles, Paths.get(bucketNameAws + "_" + Constants.DEFAULT_SAVE_STATE_DIRECTORY));
    }

    private boolean needToProcessBucketItem(BucketItem b) {
        long fileSizeInMs = bucketOneDrive.getFileInfo(b.getPath()).getSize();
        //        if (fileSizeInMs < Constants.PRINTABLE_CHUNK_SIZE) {
        boolean processFile = ((fileSizeInMs < 0) || (fileSizeInMs != b.getSize())) || processedFiles
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(b.getPath()))
                .anyMatch(e -> e.getValue() != b.getSize());

        logger.info("Check if present: {}; file in cloud {}, {} vs {}", !processFile, b.getPath(), b.getSize(), fileSizeInMs);
        return processFile;
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
