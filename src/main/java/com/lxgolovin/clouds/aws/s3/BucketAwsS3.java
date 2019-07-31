package com.lxgolovin.clouds.aws.s3;

import com.lxgolovin.clouds.aws.client.Client;
import com.lxgolovin.clouds.config.Configuration;
import com.lxgolovin.clouds.config.Constants;
import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Objects.isNull;

public class BucketAwsS3 {

    private S3Client s3;

    private Set<BucketItem> bucketItems;

    private final String bucket;

    private long bucketSizeTotal;

    private long filesCount;

    private final Map<String, Set<BucketItem>> bucketsStateMap;

    private Logger logger = LoggerFactory.getLogger(BucketAwsS3.class);

    public BucketAwsS3(String bucket, String prefix) {
        this(Client.getS3Client(), bucket, prefix);
    }

    BucketAwsS3(S3Client s3Client, String bucketName, String prefix) {
        ifIllegalNull(bucketName, "Bucket name cannot be null");

        this.s3 = (s3Client == null) ? Client.getS3Client() : s3Client;
        this.bucket = bucketName;

        bucketsStateMap = readBucketState();
        initBucket(prefix);
    }

    private void saveBucketState() {
        // TODO: need to put to separate class
        Path path = Paths.get(new Configuration().getAwsBucketStatePath());
        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {

            objectOutputStream.writeObject(bucketsStateMap);
            objectOutputStream.flush();
        } catch (IOException e) {
            logger.error("Cannot save state for the bucket items: {}", e.getLocalizedMessage());
            throw new IllegalAccessError();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Set<BucketItem>> readBucketState() {
        Map<String, Set<BucketItem>> map;
        Path path = Paths.get(new Configuration().getAwsBucketStatePath());

        try (InputStream inputStream = Files.newInputStream(path);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            map = (Map<String, Set<BucketItem>>) objectInputStream.readObject();
        } catch (Exception e) {
            logger.info("Cannot read state for the bucket items {}", e.getLocalizedMessage());
            map = new HashMap<>();
        }

        return map;
    }

    private void initBucket(String prefix) {
        if (bucketsStateMap.containsKey(prefix)) {
            initPreSavedBucket(prefix);
        } else {
            initRealTimeBucket(prefix);
        }
        filesCount = bucketItems
                .stream()
                .filter(BucketItem::isFile)
                .count();
    }

    private void initRealTimeBucket(String prefix) {
        // TODO: filter will be implemented: String contentFilter = (isNull(filter)) ? Constants.DEFAULT_CLOUD_FS_FILTER : filter;
        String contentFilter = Constants.DEFAULT_CLOUD_FS_FILTER;
        this.bucketItems = new HashSet<>();
        this.bucketSizeTotal = 0;

        logger.debug("Initialize bucket {} with prefix {}", this.bucket, prefix);
        ListObjectsV2Request listReq = ListObjectsV2Request
                .builder()
                .bucket(bucket)
                .prefix(prefix)
                .maxKeys(1)
                .build();

        try {
            s3.listObjectsV2Paginator(listReq)
                    .stream()
                    .flatMap(r -> r.contents().stream())
                    .filter(f -> f.key().matches(contentFilter))
                    .forEach(content -> {
                        boolean isFolder = content.key().matches(Constants.REGEX_IS_FOLDER);
                        BucketItem bucketItem = new BucketItem(content.key(), content.size(), !isFolder);
                        this.bucketItems.add(bucketItem);
                        bucketSizeTotal += content.size();
                    });
            //if (!bucketsStateMap.containsKey(prefix)) {
            bucketsStateMap.put(prefix, bucketItems);
            saveBucketState();
            //}
        } catch (AwsServiceException e) {
            logger.error("Cannot read bucket {}: message {}, code: {}", bucket, e.toBuilder().message(), e.toBuilder().statusCode());
        } catch (SdkClientException e) {
            logger.error("Cannot read bucket {}: {}", bucket, e.getLocalizedMessage());
        }
    }

    private void initPreSavedBucket(String prefix) {
        bucketItems = bucketsStateMap.get(prefix);
        bucketSizeTotal = bucketItems
                .stream()
                .filter(BucketItem::isFile)
                .mapToLong(BucketItem::getSize)
                .sum();
    }

    public Set<BucketItem> readBucket() {
        return bucketItems;
    }

    public InputStream readBucketItem(BucketItem bucketItem) throws IllegalArgumentException {
        ifIllegalNull(bucketItem, "Bucket item cannot be null");

        if (!bucketItems.contains(bucketItem)) {
            throw new IllegalArgumentException("Item is not present in the bucket");
        }

//        if (!bucketItem.isFile()) {
//            return null;
//        }

        InputStream targetInputStream = null;
        try {
            ResponseInputStream<GetObjectResponse> responseResponseInputStream = getResponseResponseInputStream(bucketItem);
            long contentLength = responseResponseInputStream.response().contentLength();

//            targetInputStream = getInputStreamBuffered(responseResponseInputStream, contentLength);
            targetInputStream = getInputStreamBuffered(responseResponseInputStream, contentLength);
            logger.debug("Buffering finished. Size {}", bucketItem.getSize());
        } catch (AwsServiceException e) {
            logger.error("Cannot read bucket item {}: message {}, code: {}", bucketItem.getPath(), e.toBuilder().message(), e.toBuilder().statusCode());
            if (e.toBuilder().statusCode() == Constants.AWS_TOKEN_EXPIRED) {
                this.s3 = Client.getS3Client();
            }
        } catch (SdkClientException e) {
            logger.error("Cannot read bucket item {}: {}", bucketItem.getPath(), e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error("IO error. cannot read incoming data. {}", e.getLocalizedMessage());
        } catch (OutOfMemoryError e) {
            logger.error("File is too large {}MB. Not supported yet: {}", bucketItem.getSize()/(1024*1024), e.getLocalizedMessage());
        }

        return targetInputStream;
    }

    private InputStream getInputStreamBuffered(ResponseInputStream<GetObjectResponse> responseResponseInputStream, long contentLength)
            throws IOException, OutOfMemoryError {
        BufferedInputStream bis = new BufferedInputStream(responseResponseInputStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//        if (contentLength > 1*1024*1024*1024) { // GB
//            return null;
//        }

        byte[] buffer = new byte[Constants.DOWNLOAD_BUFFER_SIZE];
        int len;
        int byteReadCount = 0;
        int byteReadCountToPrint = 0;

        logger.info("Read done: 0 of {}", contentLength);
        while (true) {
            len = bis.read(buffer);
            if (len == -1) {
                break;
            }

            byteReadCount += len;
            byteReadCountToPrint += len;
            if (byteReadCountToPrint > Constants.PRINTABLE_CHUNK_SIZE) {
                logger.info("Reading done: {} of {}", byteReadCount, contentLength);
                byteReadCountToPrint = 0;
            }
            baos.write(buffer, 0, len);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private InputStream getInputStream(InputStream inputStream, long size) throws IOException, OutOfMemoryError {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<InputStream> dataSet = new ArrayList<>();

        byte[] buffer = new byte[Constants.DOWNLOAD_BUFFER_SIZE];
        int len;
        int byteReadCount = 0;
        int byteReadCountPrintable = 0;

        logger.info("Read done: 0 of {}", size);
        while (true) {
            len = bis.read(buffer);
            if (len == -1) {
                break;
            }

            byteReadCount += len;
            byteReadCountPrintable += len;
            if (byteReadCountPrintable > Constants.PRINTABLE_CHUNK_SIZE) {
                logger.info("Reading done: {} of {}", byteReadCount, size);
                byteReadCountPrintable = 0;
            }

            baos.write(buffer, 0, len);
            if (baos.size() > (Constants.MAX_BUFFER_SIZE-Constants.DOWNLOAD_BUFFER_SIZE)) {
                dataSet.add(new ByteArrayInputStream(baos.toByteArray()));
                baos = new ByteArrayOutputStream();
            }
        }
        dataSet.add(new ByteArrayInputStream(baos.toByteArray()));
        return new SequenceInputStream(Collections.enumeration(dataSet));
    }

    private ResponseInputStream<GetObjectResponse> getResponseResponseInputStream(BucketItem bucketItem)
            throws AwsServiceException, SdkClientException {
        return s3.getObject(b -> b.bucket(bucket).key(bucketItem.getPath()), ResponseTransformer.toInputStream());
    }

    boolean saveBucketItem(BucketItem bucketItem) {
        return this.saveBucketItem(bucketItem, null);
    }

    boolean saveBucketItem(BucketItem bucketItem, String targetDir) {
        ifIllegalNull(bucketItem, "Source item cannot be null");
        if (!bucketItems.contains(bucketItem)) {
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
        } catch (AwsServiceException e) {
            logger.error("Cannot save item {}: message {}, code: {}", sourcePath, e.toBuilder().message(), e.toBuilder().statusCode());
        } catch (SdkClientException e) {
            logger.error("Cannot save bucket item {}: {}", sourcePath, e.getLocalizedMessage());
        } catch (IOException | SdkException e) {
            logger.error("Cannot save file '{}' to file '{}': File exists {}", sourcePath, saveAs, e.getLocalizedMessage());
        }

        return isSaved;
    }

    private boolean processSaveFile(String sourceFile, String saveAs) throws IOException, AwsServiceException, SdkClientException {
        Path path = Paths.get(saveAs);
        if (Files.exists(path)) {
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

        return Files.exists(path);
    }

    private boolean createLocalFolder(String saveAs) throws IOException {
        Path path = Paths.get(saveAs);
        if (Files.exists(path)) {
            logger.warn("Target folder '{}' already present, skip source folder", saveAs);
            return false;
        }

        Files.createDirectories(path);
        return Files.exists(path);
    }

    public long filesCount() {
        return this.filesCount;
    }

    public int itemsCount() {
        return bucketItems.size();
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