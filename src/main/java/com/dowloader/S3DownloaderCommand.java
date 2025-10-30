package com.dowloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.dowloader.service.S3DownloaderService;

@Component
public class S3DownloaderCommand implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(S3DownloaderCommand.class);
    private final S3DownloaderService s3DownloaderService;

    @Value("${aws.profile:default}")
    private String awsProfile;

    @Value("${aws.region:}")
    private String awsRegion;

    @Value("${aws.bucket}")
    private String bucketName;

    @Value("${prefix:}")
    private String prefix;

    @Value("${downloadDir:./download}")
    private String downloadDir;

    public S3DownloaderCommand(S3DownloaderService s3DownloaderService) {
        this.s3DownloaderService = s3DownloaderService;
    }

    @Override
    public void run(String... args) {
        if (prefix == null || prefix.isBlank()) {
            System.out.println("Missing required parameter: --prefix");
            printUsage();
            return;
        }

        if (awsRegion == null || awsRegion.isBlank()) {
            System.out.println("Missing required parameter: --aws.region");
            printUsage();
            return;
        }

        log.info("Starting S3 download");
        log.info("Profile: {}", awsProfile);
        log.info("Region: {}", awsRegion);
        log.info("Bucket: {}", bucketName);
        log.info("Prefix: {}", prefix);
        log.info("Download directory: {}", downloadDir);

        try {
            s3DownloaderService.downloadRecursively(awsProfile, awsRegion, bucketName, prefix, downloadDir);
            log.info("Download completed successfully");
        } catch (Exception e) {
            log.error("Error during download: {}", e.getMessage());
        }
    }

    private void printUsage() {
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar s3-downloader.jar \\");
        System.out.println("    --aws.bucket=<bucket-name> \\");
        System.out.println("    --aws.region=<region> \\");
        System.out.println("    [--aws.profile=<profile-name>] \\");
        System.out.println("    --prefix=<s3-prefix> \\");
        System.out.println("    [--downloadDir=./download]");
        System.out.println();
    }
}
