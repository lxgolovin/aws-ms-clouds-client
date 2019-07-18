package com.lxgolovin.aws.s3;

import com.lxgolovin.aws.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.isNull;

public class Bucket {

    private final S3Client s3;

    private Set<FileNode> s3Bucket;

    private final String bucket;

    private long bucketSizeTotal;

    private Logger logger = LoggerFactory.getLogger(Bucket.class);

    public Bucket(String bucket) {
        this(null, bucket);
    }

    Bucket(S3Client s3Client, String bucket) {
        isIllegalNull(bucket);

        this.s3 = (s3Client == null) ? Client.getS3Client() : s3Client;
        this.bucket = bucket;

        logger.debug("Start to work with new bucket {}", this.bucket);
        readBucket(null);
    }

    public Set<FileNode> readBucket() {
        return readBucket(null);
    }

    public Set<FileNode> readBucket(String filter) {
        String regex = (isNull(filter)) ? ".*" : filter;
        this.bucketSizeTotal = 0;
        this.s3Bucket = new HashSet<>();

        ListObjectsV2Request listReq = ListObjectsV2Request
                .builder()
                .bucket(bucket)
                .maxKeys(1)
                .build();

        try {
            s3.listObjectsV2Paginator(listReq)
                    .stream()
                    .flatMap(r -> r.contents().stream())
                    .filter(f -> f.key().matches(regex))
                    .forEach(content -> {
                        boolean isFolder = content.key().matches(".*/$");
                        FileNode fileNode = new FileNode(content.key(), content.size(), isFolder);
                        this.s3Bucket.add(fileNode);
                        bucketSizeTotal += content.size();
                    });
        } catch (SdkException e) {
            logger.error("Cannot read bucket {}: {}", bucket, e.getLocalizedMessage());
        }
        return s3Bucket;
    }

    public InputStream getFile(FileNode file) {
        isIllegalNull(file);
        if (!s3Bucket.contains(file)) {
            throw new IllegalArgumentException();
        }

        InputStream inputStream = null;

        if (!file.isFolder()) {
            inputStream = s3.getObject(GetObjectRequest
                            .builder()
                            .bucket(bucket)
                            .key(file.getPath())
                            .build(),
                    ResponseTransformer.toInputStream());
        }

        return inputStream;
    }

    public boolean saveFileLocally(FileNode file) {
        return this.saveFileLocally(file, null);
    }

    boolean saveFileLocally(FileNode source, String targetPath) {
        isIllegalNull(source);
        if (!s3Bucket.contains(source)) {
            throw new IllegalArgumentException();
        }

        String sourcePath = source.getPath();
        boolean isSaved = false;
        String saveAs = (isNull(targetPath)) ? sourcePath : targetPath;

        try {
            if (source.isFolder()) {
                logger.debug("Processing source folder '{}' to target folder '{}'", sourcePath, saveAs);
                isSaved = createLocalFolder(saveAs);
            } else {
                logger.debug("Processing source file '{}' to target file '{}'", sourcePath, saveAs);
                isSaved = processSaveFile(sourcePath, saveAs);
            }
        } catch (IOException | SdkException e) {
            logger.error("Cannot save file '{}' to file '{}': File exists {}", sourcePath, saveAs, e.getLocalizedMessage());
        }

        return isSaved;
    }

    private boolean processSaveFile(String sourceFile, String saveAs) throws IOException {
        if (Files.exists(Paths.get(saveAs))) {
            logger.warn("Target file '{}' already present, skip source file", saveAs);
            return false;
        }

        String folder = FileNode.getPathToFile(saveAs);
        createLocalFolder(folder);

        GetObjectRequest getObjectRequest = GetObjectRequest
                .builder()
                .bucket(bucket)
                .key(sourceFile)
                .build();

        s3.getObject(getObjectRequest, ResponseTransformer.toFile(Paths.get(saveAs)));
        logger.debug("File '{}' saved to file '{}'", sourceFile, saveAs);
        return true;
    }

    private boolean createLocalFolder(String saveAs) throws IOException {
        if (Files.exists(Paths.get(saveAs))) {
            logger.warn("Target folder '{}' already present, skip source folder", saveAs);
            return false;
        } else {
            Files.createDirectories(Paths.get(saveAs));
            logger.debug("Folder '{}' created", saveAs);
            return true;
        }
    }

    public int filesCount() {
        return s3Bucket.size();
    }

    public long sizeTotalBytes() {
        return bucketSizeTotal;
    }

    private void isIllegalNull(Object o) {
        if (isNull(o)) {
            throw new IllegalArgumentException();
        }
    }
}