package com.lxgolovin.clouds.tasks;

public class Task {
    private final String sourceBucketName;
    private final String sourceBucketFilter;
    private final String targetBucketName;

    Task(String sourceBucket, String targetBucket, String filter) {
        this.sourceBucketName = sourceBucket;
        this.sourceBucketFilter = (filter == null) ? ".*" : filter;
        this.targetBucketName = targetBucket;
    }
}
