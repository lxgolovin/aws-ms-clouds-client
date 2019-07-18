package com.lxgolovin.aws.client;

import com.lxgolovin.aws.s3.Bucket;
import com.lxgolovin.aws.s3.BucketManager;
import com.lxgolovin.aws.s3.FileNode;
import com.lxgolovin.msgraph.filesystem.File;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static final String DEFAULT_BUCKET_NAME = "aws-bucket-test-new-1";
    // private static final String DEFAULT_BUCKET_NAME = "company-saleselm-10001";

    public static void main(String[] args) {

        Options options = new Options();

        Option listBucketsOpt = new Option("l", "list-buckets", false, "list of buckets");
        listBucketsOpt.setRequired(false);
        options.addOption(listBucketsOpt);

        Option bucketOpt = new Option("b", "bucket", true, "print content of the bucket");
        bucketOpt.setRequired(false);
        options.addOption(bucketOpt);

        Option copyOpt = new Option("c", "copy", true, "file to copy from local drive");
        copyOpt.setRequired(false);
        options.addOption(copyOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("l")) {
                // S3Client s3 = S3Client.builder().region(Region.EU_CENTRAL_1).build();
                // BucketManager bucketManager = new BucketManager(s3);
                BucketManager bucketManager = new BucketManager();
                logger.debug("List buckets:");
                bucketManager
                        .listBuckets()
                        .forEach(b ->
                                logger.debug("Bucket: {}, location {}", b.name(), bucketManager.getLocation(b.name())));
            } else if (cmd.hasOption("c")) {
                String fileName = cmd.getOptionValue("copy");
                String oneDriveBucket = "01XHM6HBS53JZKPP326VFIRAMG4H56PBXH";
                try (InputStream uploadFileStream = Files.newInputStream(Paths.get(fileName))) {
                    File file = new File(oneDriveBucket);
                    file.upload(uploadFileStream, fileName);
                } catch (IOException e) {
                    throw new Error("File cannot be accessed: " + e.getLocalizedMessage());
                }
            } else {
                String bucketName = DEFAULT_BUCKET_NAME;
                if (cmd.hasOption("b")) {
                    bucketName = cmd.getOptionValue("bucket");
                }

                Bucket bucket = new Bucket(bucketName);
                logger.debug("Read bucket '{}':", bucketName);
                logger.debug("Number of items: {}", bucket.filesCount());
                logger.debug("Bytes total: {}", bucket.sizeTotalBytes());

                Set<FileNode> fileNodes = bucket.readBucket(null);
                fileNodes.forEach(fn -> {
                    logger.debug("Path: '{}'; Size: {}; IsFolder: '{}'", fn.getPath(), fn.getSize(), fn.isFolder());


                });
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("aws-java-client", options);
            System.exit(1);
        }
    }
}
