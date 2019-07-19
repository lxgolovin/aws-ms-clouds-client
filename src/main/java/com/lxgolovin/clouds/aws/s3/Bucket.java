package com.lxgolovin.clouds.aws.s3;

import com.lxgolovin.clouds.aws.client.Client;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.msgraph.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.isNull;

public class Bucket {

    private final S3Client s3;

    private Set<BucketItem> s3Bucket;

    private final String bucket;

    private long bucketSizeTotal;

    private Logger logger = LoggerFactory.getLogger(Bucket.class);

    public Bucket(String bucket) {
        this(Client.getS3Client(), bucket);
    }

    Bucket(S3Client s3Client, String bucketName) {
        ifIllegalNull(bucketName, "Bucket name cannot be null");

        this.s3 = (s3Client == null) ? Client.getS3Client() : s3Client;
        this.bucket = bucketName;

        logger.debug("Initialize bucket {}", this.bucket);
        readBucket(null);
    }

    Set<BucketItem> readBucket() {
        return readBucket(null);
    }

    public Set<BucketItem> readBucket(String filter) {
        String contentFilter = (isNull(filter)) ? ".*" : filter;
        this.s3Bucket = new HashSet<>();
        this.bucketSizeTotal = 0;

        ListObjectsV2Request listReq = ListObjectsV2Request
                .builder()
                .bucket(bucket)
                .maxKeys(1)
                .build();

        try {
            s3.listObjectsV2Paginator(listReq)
                    .stream()
                    .flatMap(r -> r.contents().stream())
                    .filter(f -> f.key().matches(contentFilter))
                    .forEach(content -> {
                        boolean isFolder = content.key().matches(".*/$");
                        BucketItem bucketItem = new BucketItem(bucket, content.key(), content.size(), !isFolder);
                        this.s3Bucket.add(bucketItem);
                        bucketSizeTotal += content.size();
                    });
        } catch (SdkException e) {
            logger.error("Cannot read bucket {}: {}", bucket, e.toBuilder().message());
        }
        return s3Bucket;
    }

    public InputStream readBucketItem(BucketItem bucketItem) {
        ifIllegalNull(bucketItem, "Bucket item cannot be null");

        if (!s3Bucket.contains(bucketItem)) {
            throw new IllegalArgumentException("Item is not present in the bucket");
        }

        if (!bucketItem.isFile()) {
            return null;
        }

        InputStream targetInputStream;
        ResponseInputStream<GetObjectResponse> responseResponseInputStream = getResponseResponseInputStream(bucketItem);
        int contentLength = responseResponseInputStream.response().contentLength().intValue();

        logger.debug("Buffering file '{}'", bucketItem.getPath());
        if (contentLength < Constants.MAXIMUM_CHUNK_SIZE) {
            targetInputStream = getInputStream(responseResponseInputStream, contentLength);
        } else {
            targetInputStream = new BufferedInputStream(responseResponseInputStream, Constants.DEFAULT_CHUNK_SIZE);
        }
        logger.debug("File {} buffered", bucketItem.getPath());

        return targetInputStream;
    }

    private InputStream getInputStream(InputStream inputStream, int size) {
        byte[] buffer = new byte[size];
        try {
            int b = 0;
            while (b != -1) {
                b = inputStream.read(buffer);
            }
        } catch (IOException e) {
            logger.error("Buffer IO error. {}", e.getLocalizedMessage());
        }
        return new ByteArrayInputStream(buffer);
    }

    private ResponseInputStream<GetObjectResponse> getResponseResponseInputStream(BucketItem bucketItem) {
        // TODO: to handle exceptions
        return s3.getObject(b ->
                b.bucket(bucket).key(bucketItem.getPath()),
                ResponseTransformer.toInputStream());
    }

    boolean saveBucketItem(BucketItem bucketItem) {
        return this.saveBucketItem(bucketItem, null);
    }

    boolean saveBucketItem(BucketItem bucketItem, String targetDir) {
        ifIllegalNull(bucketItem, "Source item cannot be null");
        if (!s3Bucket.contains(bucketItem)) {
            throw new IllegalArgumentException("Item is not present in the bucket");
        }

        String sourcePath = bucketItem.getPath();
        String saveAs = (isNull(targetDir)) ? sourcePath : targetDir.concat("/").concat(sourcePath);

        boolean isSaved = false;
        logger.debug("Processing source item '{}'", sourcePath);
        try {
            if (bucketItem.isFile()) {
                isSaved = processSaveFile(sourcePath, saveAs);
            } else {
                isSaved = createLocalFolder(saveAs);
            }
        } catch (IOException | SdkException e) {
            logger.error("Cannot save file '{}' to file '{}': File exists {}", sourcePath, saveAs, e.getLocalizedMessage());
        }

        return isSaved;
    }

    private boolean processSaveFile(String sourceFile, String saveAs) throws IOException {
        if (Files.exists(Paths.get(saveAs))) {
            logger.warn("Target file '{}' already present, skip source file {}", saveAs, sourceFile);
            return false;
        }

        String folder = BucketItem.getParentFolder(saveAs);
        createLocalFolder(folder);

        GetObjectRequest getObjectRequest = GetObjectRequest
                .builder()
                .bucket(bucket)
                .key(sourceFile)
                .build();

        s3.getObject(getObjectRequest, ResponseTransformer.toFile(Paths.get(saveAs)));
        return true;
    }

    private boolean createLocalFolder(String saveAs) throws IOException {
        boolean isCreated = false;

        if (Files.exists(Paths.get(saveAs))) {
            logger.warn("Target folder '{}' already present, skip source folder", saveAs);
        } else {
            Files.createDirectories(Paths.get(saveAs));
            isCreated = true;
        }

        return isCreated;
    }

    public int filesCount() {
        return s3Bucket.size();
    }

    public long sizeTotalBytes() {
        return bucketSizeTotal;
    }

    private void ifIllegalNull(Object object, String message) {
        if (isNull(object)) {
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}