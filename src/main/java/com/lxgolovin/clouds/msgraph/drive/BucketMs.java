package com.lxgolovin.clouds.msgraph.drive;

import com.lxgolovin.clouds.filesystem.DriveNode;
import com.lxgolovin.clouds.msgraph.auth.AuthenticateInsecure;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

public class BucketMs {

    private Set<DriveNode> msBucket;

    private final String bucket;

    private final IGraphServiceClient graphClient;

    private Logger logger = LoggerFactory.getLogger(File.class);

    public BucketMs(String bucket) {
        this(null, bucket);
    }

    BucketMs(IGraphServiceClient graphClient, String bucket) {
        if (bucket == null) {
            throw new IllegalArgumentException();
        }

        this.bucket = bucket;
        this.graphClient = (graphClient == null) ? AuthenticateInsecure.initGraphClient() : graphClient;
    }

    public Set<DriveNode> readBucket() {
        return readBucket(null);
    }

    public Set<DriveNode> readBucket(String filter) {
        String regex = (isNull(filter)) ? ".*" : filter;
        this.msBucket = new HashSet<>();
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
                    .forEach(di -> msBucket.add(File.msDriveItemToNode(di)));
        } catch (ClientException e) {
            logger.error("Cannot read bucket {}: {}", bucket, e.getLocalizedMessage());
        }

        return msBucket;
    }
}
