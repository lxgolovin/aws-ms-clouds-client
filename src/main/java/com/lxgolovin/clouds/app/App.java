package com.lxgolovin.clouds.app;

import com.lxgolovin.clouds.aws.s3.BucketManager;
import com.lxgolovin.clouds.tasks.Copier;
import com.lxgolovin.clouds.msgraph.drive.BucketOneDrive;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

//        private static final String DEFAULT_BUCKET_NAME = "aws-bucket-test-new-1";
//        private static final String DEFAULT_BUCKET_NAME = "aws-nonprod-alm-oneview";
//    private static final String DEFAULT_BUCKET_NAME = "aws-nonprod-alm-oneview";
//    private static final String DEFAULT_BUCKET_NAME = "company-saleselm-10001";
    private static final String DEFAULT_BUCKET_NAME = "aws-prod-vault-content";

    private static final String DEFAULT_MS_BUCKET_NAME = "01XHM6HBUOKVNQM2MZERGLFEJ3FITM4CTP";
    private static final String prefix = "GECMS/NONPRODUCT_CONTENTS/Image Archive";
    private static final String filter = ".*";

    public static void main(String[] args) {

//        String proxy = "http://proxy-qa.aws.wiley.com";
//        String proxy = "proxy-dev.aws.wiley.com";
//        String port = "8080";
//        System.setProperty("http.proxyHost", proxy);
//        System.setProperty("https.proxyHost", proxy);
//        System.setProperty("http.proxyPort", port);
//        System.setProperty("https.proxyPort", port);
        System.setProperty("aws.accessKeyId", "ASIAUKQAU3HFMK47INWC");
        System.setProperty("aws.secretAccessKey", "FRdFizoyAtqmggb6DQNmKBkJ+rwXGK95HNsfFsJW");
        System.setProperty("aws.securityToken", "FQoGZXIvYXdzEIj//////////wEaDMVuX7dW+ZCyO65o1iLzAX8wFYS164X/6lxxGtPr0f6xIERZrCqpUQVlW1rT3gAUAGmzC+oR1xigphK+QukXq6YEGOgnvbX0QP6VIIM0FwG/XVIQI7A5jUlNisI6M8fS85dVEdgwdeLzOCSsBej8tlSkhkwoUnKri4s11VjMZEHfdfap7AtgRtsmDN7oFgE+704ExIPx8h8GyJnEZUUjzlJ7N6BYdLzoc13RmtO8WYIt4iaR3WD9aeQb1sxY5JTll9d33nFRo8E1FAiQ4QlGl+JA8CRQyDE1Nx6Qjq1hYJA7+MdvTfybwRsLxmSSArZEKubmzvo0bDHFuJgG1Wh22tHCbSidkN7pBQ==");


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        Options options = initCliOptions();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("l")) {
                logger.info("List buckets:");
                new BucketManager()
                        .listBuckets()
                        .forEach(b ->
                                logger.debug("BucketAwsS3: {}", b.name()));
            } else if (cmd.hasOption("B")) {
                String bucketName = DEFAULT_MS_BUCKET_NAME;
                logger.debug("Read bucket '{}':", bucketName);
                new BucketOneDrive(bucketName)
                        .readBucket()
                        .forEach(fn ->
                                logger.debug("Path: '{}'; Size: {}; IsRegularFile: '{}'", fn.getPath(), fn.getSize(), fn.isFile()));
            } else {
                String bucketName = (cmd.hasOption("b")) ? cmd.getOptionValue("bucket") : DEFAULT_BUCKET_NAME;
                new Copier(bucketName, DEFAULT_MS_BUCKET_NAME, prefix).copyAwsToMs(filter);
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("aws-java-client", options);
            System.exit(1);
        }
    }

    private static Options initCliOptions() {
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

        return options;
    }


}
