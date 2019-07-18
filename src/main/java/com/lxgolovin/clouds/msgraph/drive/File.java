package com.lxgolovin.clouds.msgraph.drive;

import com.lxgolovin.clouds.filesystem.DriveNode;
import com.lxgolovin.clouds.msgraph.auth.AuthenticateInsecure;
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

public class File {

    private final String bucket;

    private final IGraphServiceClient graphClient;

    private Logger logger = LoggerFactory.getLogger(File.class);

    public File(String bucket) {
        this(null, bucket);
    }

    File(IGraphServiceClient graphClient, String bucket) {
        if (bucket == null) {
            throw new IllegalArgumentException();
        }

        this.bucket = bucket;
        this.graphClient = (graphClient == null) ? AuthenticateInsecure.initGraphClient() : graphClient;
    }

    public DriveNode getFileInfo(String file) {
        DriveItem resultDriveItem = null;
        try {
            resultDriveItem =  graphClient
                    .me()
                    .drive()
                    .items(bucket)
                    .itemWithPath(file)
                    .buildRequest()
                    .get();
        } catch (GraphServiceException e) {
            logger.error("Cannot get file {} info: {}", file, e.getResponseMessage());
        }

        return msDriveItemToNode(resultDriveItem);
    }

    static DriveNode msDriveItemToNode(DriveItem resultDriveItem) {
        DriveNode driveNode = null;
        if (resultDriveItem != null) {
            String path = resultDriveItem.parentReference.path.replaceAll("(/drive/root:)", "");
            path = path.concat("/").concat(resultDriveItem.name);
            String parentBucket = resultDriveItem.parentReference.driveId;
            boolean isFolder = (resultDriveItem.folder != null);

            driveNode = new DriveNode(
                    parentBucket,
                    path,
                    resultDriveItem.size,
                    isFolder);
        }
        return driveNode;
    }

    public boolean delete(String file) {
        boolean deleteResult = false;
        try {
            graphClient
                    .me()
                    .drive()
                    .items(bucket)
                    .itemWithPath(file)
                    .buildRequest()
                    .delete();
            deleteResult = true;
        } catch (GraphServiceException e) {
            logger.error("Cannot delete file {} info: {}", file, e.getResponseMessage());
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
        int fileSize;

        try {
            fileSize = inputStream.available();

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
            uploadResult = true;
        } catch (IOException e) {
            logger.error("IO error during file upload {}. Try later. {}", fileName, e.getLocalizedMessage());
        }
        return uploadResult;
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
