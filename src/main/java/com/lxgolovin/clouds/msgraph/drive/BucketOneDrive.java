package com.lxgolovin.clouds.msgraph.drive;

import com.lxgolovin.clouds.cloudfs.core.BucketItem;
import com.lxgolovin.clouds.config.Constants;
import com.lxgolovin.clouds.msgraph.client.Client;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.UploadSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BucketOneDrive {

    private Set<BucketItem> bucketItems;

    private final String bucket;

    private final IGraphServiceClient graphClient;

    private Logger logger = LoggerFactory.getLogger(BucketOneDrive.class);

    public BucketOneDrive(String bucket) {
        this(null, bucket);
    }

    BucketOneDrive(IGraphServiceClient graphClient, String bucket) {
        if (bucket == null) {
            throw new IllegalArgumentException();
        }

        this.bucket = bucket;
        this.graphClient = (graphClient == null) ? Client.getMsGraphClient() : graphClient;
    }

    public Set<BucketItem> readBucket() {
        // TODO: need to implement filter as a param: String regex = (isNull(filter)) ? Constants.DEFAULT_FILTER : filter;
        final String regex = Constants.DEFAULT_FILTER;
        this.bucketItems = new HashSet<>();
        try {
            List<DriveItem> driveItems =  graphClient
                    .me()
                    .drive()
                    .items(bucket)
                    .children()
                    .buildRequest()
                    .get()
                    .getCurrentPage();

            driveItems.stream()
                    .filter(driveItem -> driveItem.name.matches(regex))
                    .forEach(driveItem -> {
                        BucketItem bucketItem = msDriveItemToNode(driveItem);
                        bucketItems.add(bucketItem);
                    });
        } catch (ClientException e) {
            logger.error("Cannot read bucket {}: {}", bucket, e.getLocalizedMessage());
        }

        return bucketItems;
    }

    public BucketItem getFileInfo(String file) {
        BucketItem bucketItem = null;

        try {
            bucketItem = msDriveItemToNode(graphClient
                    .me()
                    .drive()
                    .items(bucket)
                    .itemWithPath(pathToUrl(file))
                    .buildRequest()
                    .get());
        } catch (GraphServiceException e) {
            if (e.getResponseCode() == Constants.HTTP_RESPONSE_NOT_FOUND) {
                logger.info("Not found: File '{}'. Response code: {}; system response: {}",
                        file, e.getResponseCode(), e.getResponseMessage());
            } else {
                logger.error("Cannot get file {} info: {}", file, e.getResponseMessage());
            }
        } catch (ClientException e) {
            logger.error("Not able to read file info: {} : {}", file, e.getLocalizedMessage());
        }

        return (bucketItem == null) ? new BucketItem(file) : bucketItem;
    }

    private String pathToUrl(String file) {
        if (file == null) {
            return null;
        }

        String fileName = file;
        try {
            fileName = URLEncoder.encode(file, StandardCharsets.UTF_8.toString());
            fileName = fileName.replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.error("A character was not found in UTF-8 in file path {}: {}", file, e.getLocalizedMessage());
        }
        return fileName;
    }

    private BucketItem msDriveItemToNode(DriveItem driveItem) {
        BucketItem bucketItem = null;
        if (driveItem != null) {
            String path = driveItem.parentReference.path.replaceAll("(/drive/root:)", "");
            path = path.concat("/").concat(driveItem.name);
            // TODO: to be implemented in future: String parentBucket = resultDriveItem.parentReference.driveId;
            boolean isFolder = (driveItem.folder == null);

            bucketItem = new BucketItem(
                    path,
                    driveItem.size,
                    isFolder);
        }
        return bucketItem;
    }

    public boolean delete(String fileName) {
        boolean deleteResult = false;
        try {
            graphClient
                    .me()
                    .drive()
                    .items(bucket)
                    .itemWithPath(pathToUrl(fileName))
                    .buildRequest()
                    .delete();
            deleteResult = true;
        } catch (GraphServiceException e) {
            if (e.getResponseCode() == Constants.HTTP_RESPONSE_NOT_FOUND) {
                logger.info("Not deleted: File '{}'. Response code: {}; system response: {}",
                        fileName, e.getResponseCode(), e.getResponseMessage());
            } else {
                logger.error("Cannot delete file {} info: {}", fileName, e.getResponseMessage());
            }
        }

        return deleteResult;
    }

    public boolean upload(InputStream inputStream, String fileName) {
        boolean isUploaded = false;
        try {
            int retry = 0;
            while (!isUploaded | (retry < Constants.RETRY_TIMES)) {
                isUploaded = upload(inputStream, fileName, inputStream.available());
                retry++;
            }
        } catch (IOException e) {
            logger.error("IO error during file upload {}. Try later. {}", fileName, e.getLocalizedMessage());
        }

        return isUploaded;
    }

    private boolean upload(InputStream inputStream, String fileName, int fileSize) {
        if (fileName == null)  {
            throw new IllegalArgumentException();
        }

        if ((inputStream == null) || (fileSize <= 0)) {
            logger.error("No input data. Check connection. File: {}, size {}", fileName, fileSize);
            return false;
        }

        boolean isUploaded = false;
        try {
            if (fileSize <= Constants.ONE_DRIVE_MAX_CONTENT_SIZE) {
                logger.debug("Do NOT create session for file {} size {}", fileName, fileSize);
                uploadSmallFile(inputStream, fileName, fileSize);
            } else {
                logger.debug("Create session for file {} size {}", fileName, fileSize);
                uploadLargeFile(inputStream, fileName, fileSize);
            }
            isUploaded = true;
        } catch (GraphServiceException e) {
            logger.error("Not uploaded: File '{}'. Response code: {}; system response: {}",
                    fileName, e.getResponseCode(), e.getResponseMessage());
        } catch (ClientException e) {
            logger.error("Unable to upload file {}. {}", fileName, e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error("IO error during file upload {}. Try later. {}", fileName, e.getLocalizedMessage());
        }

        return isUploaded;
    }

    private void uploadLargeFile(InputStream inputStream, String fileName, int fileSize) throws IOException {
        UploadSession uploadSession = graphClient
                .me()
                .drive()
                .items(bucket)
                .itemWithPath(pathToUrl(fileName))
                .createUploadSession(new DriveItemUploadableProperties())
                .buildRequest()
                .post();

        ChunkedUploadProvider<DriveItem> chunkedUploadProvider = new ChunkedUploadProvider<>(
                uploadSession,
                graphClient,
                inputStream,
                fileSize,
                DriveItem.class);
        chunkedUploadProvider.upload(callback);
    }

    private void uploadSmallFile(InputStream inputStream, String fileName, int fileSize) throws IOException {
        byte[] buffer = new byte[fileSize];
        int b = 0;
        while (b != -1) {
            b = inputStream.read(buffer);
        }

        graphClient
                .me()
                .drive()
                .items(bucket)
                .itemWithPath(pathToUrl(fileName))
                .content()
                .buildRequest()
                .put(buffer);
    }

    private final IProgressCallback<DriveItem> callback = new IProgressCallback<DriveItem> () {

        @Override
        public void progress(final long current, final long max) {
            logger.debug("Uploaded {} of {}", current, max);
        }

        @Override
        public void success(final DriveItem result) {
            assert result.id != null;
            logger.info("File {} uploaded successfully", result.name);
        }

        @Override
        public void failure(final ClientException ex) {
            logger.error("File failed to upload. {}", ex.getLocalizedMessage());
        }
    };
}
