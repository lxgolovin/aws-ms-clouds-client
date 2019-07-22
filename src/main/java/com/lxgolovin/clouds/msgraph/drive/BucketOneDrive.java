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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

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
        return readBucket(null);
    }

    public Set<BucketItem> readBucket(String filter) {
        String regex = (isNull(filter)) ? ".*" : filter;
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
                    .filter(di -> di.name.matches(regex))
                    .forEach(di -> bucketItems.add(msDriveItemToNode(di)));
        } catch (ClientException e) {
            logger.error("Cannot read bucket {}: {}", bucket, e.getLocalizedMessage());
        }

        return bucketItems;
    }

    public BucketItem getFileInfo(String file) {
        BucketItem bucketItem;
        try {
            bucketItem = msDriveItemToNode(graphClient
                    .me()
                    .drive()
                    .items(bucket)
                    .itemWithPath(file)
                    .buildRequest()
                    .get());
        } catch (GraphServiceException e) {
            if (e.getResponseCode() == Constants.HTTP_RESPONSE_NOT_FOUND) {
                logger.info("File '{}' not found.\n Response code: {};\n system response: {}",
                        file, e.getResponseCode(), e.getResponseMessage());
            } else {
                logger.error("Cannot get file {} info: {}", file, e.getResponseMessage());
            }
            bucketItem = new BucketItem(file); // create empty BucketItem
        }

        return bucketItem;
    }

    private BucketItem msDriveItemToNode(DriveItem resultDriveItem) {
        BucketItem bucketItem = null;
        if (resultDriveItem != null) {
            String path = resultDriveItem.parentReference.path.replaceAll("(/drive/root:)", "");
            path = path.concat("/").concat(resultDriveItem.name);
            String parentBucket = resultDriveItem.parentReference.driveId;
            boolean isFolder = (resultDriveItem.folder == null);

            bucketItem = new BucketItem(
                    parentBucket,
                    path,
                    resultDriveItem.size,
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
                    .itemWithPath(fileName)
                    .buildRequest()
                    .delete();
            deleteResult = true;
        } catch (GraphServiceException e) {
            logger.error("Cannot delete file {} info: {}", fileName, e.getResponseMessage());
        }

        return deleteResult;
    }

    public boolean upload(InputStream inputStream, String fileName) {
        if (fileName == null)  {
            throw new IllegalArgumentException();
        }

        if (inputStream == null) {
            return false;
        }

        boolean uploadResult = false;

        try {
            int fileSize = inputStream.available();

            if (fileSize <= Constants.ONE_DRIVE_MAX_CONTENT_SIZE) {
                logger.debug("Do NOT create session for file {} size {}", fileName, fileSize);
                uploadSmallFile(inputStream, fileName, fileSize);
            } else {
                logger.debug("Create session for file {} size {}", fileName, fileSize);
                uploadLargeFile(inputStream, fileName, fileSize);
            }
            uploadResult = true;
        } catch (ClientException e) {
            logger.error("Unable to upload file {}. {}", fileName, e.getLocalizedMessage());
        } catch (IOException e) {
            logger.error("IO error during file upload {}. Try later. {}", fileName, e.getLocalizedMessage());
        }
        return uploadResult;
    }

    private void uploadLargeFile(InputStream inputStream, String fileName, int fileSize) throws IOException {
        UploadSession uploadSession = graphClient
                .me()
                .drive()
                .items(bucket)
                .itemWithPath(fileName)
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
                .itemWithPath(fileName)
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
