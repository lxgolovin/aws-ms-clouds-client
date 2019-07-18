package com.lxgolovin.clouds.app;

import com.lxgolovin.clouds.aws.s3.Bucket;
import com.lxgolovin.clouds.aws.s3.BucketManager;
import com.lxgolovin.clouds.msgraph.drive.BucketMs;
import com.lxgolovin.clouds.msgraph.drive.File;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

//    private static final String DEFAULT_BUCKET_NAME = "aws-bucket-test-new-1";
     private static final String DEFAULT_BUCKET_NAME = "company-saleselm-10001";

    private static final String DEFAULT_MSBUCKET_NAME = "01XHM6HBS53JZKPP326VFIRAMG4H56PBXH";

    public static void main(String[] args) {

//        String proxy = "http://proxy-qa.aws.wiley.com";
//        String port = "8080";
//        System.setProperty("http.proxyHost", proxy);
//        System.setProperty("http.proxyPort", port);

        Options options = new Options();

        Option listBucketsOpt = new Option("l", "list-buckets", false, "list of buckets");
        listBucketsOpt.setRequired(false);
        options.addOption(listBucketsOpt);

        Option bucketOpt = new Option("b", "bucket", true, "print content of the bucket");
        bucketOpt.setRequired(false);
        options.addOption(bucketOpt);

        Option bucketMsOpt = new Option("B", "bucket-ms", false, "print content of the ms bucket");
        bucketMsOpt.setRequired(false);
        options.addOption(bucketMsOpt);

//        Option copyOpt = new Option("c", "copy", true, "file to copy from local drive");
//        copyOpt.setRequired(false);
//        options.addOption(copyOpt);

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
            } else if (cmd.hasOption("B")) {
                // String bucketName = cmd.getOptionValue("bucket-");
                String bucketName = DEFAULT_MSBUCKET_NAME;

                BucketMs bucketMs = new BucketMs(bucketName);
                logger.debug("Read bucket '{}':", bucketName);

                bucketMs.readBucket(null)
                        .forEach(fn -> logger.debug("Path: '{}'; Size: {}; IsFolder: '{}'", fn.getPath(), fn.getSize(), fn.isFolder()));
//            } else if (cmd.hasOption("c")) {
//                String fileName = cmd.getOptionValue("copy");
//                String oneDriveBucket = DEFAULT_MSBUCKET_NAME;


                /*
                try (InputStream uploadFileStream = Files.newInputStream(Paths.get(fileName))) {
                    File file = new File(oneDriveBucket);
                    file.upload(uploadFileStream, fileName);
                } catch (IOException e) {
                    throw new Error("File cannot be accessed: " + e.getLocalizedMessage());
                }

                 */
            } else {
                String bucketName = DEFAULT_BUCKET_NAME;
                if (cmd.hasOption("b")) {
                    bucketName = cmd.getOptionValue("bucket");
                }

                Bucket bucket = new Bucket(bucketName);
                logger.debug("Read bucket '{}':", bucketName);
                logger.debug("Number of items: {}", bucket.filesCount());
                logger.debug("Bytes total: {}", bucket.sizeTotalBytes());

                bucket.readBucket(null)
                        .forEach(fn -> {
                            logger.debug("Path: '{}'; Size: {}; IsFolder: '{}'", fn.getPath(), fn.getSize(), fn.isFolder());

                            String bucketMsName = DEFAULT_MSBUCKET_NAME;
                            InputStream inputStream = bucket.getFile(fn);
                            File file = new File(bucketMsName);
                            file.upload(inputStream, fn.getPath());
                        });
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("aws-java-client", options);
            System.exit(1);
        }
    }
}
