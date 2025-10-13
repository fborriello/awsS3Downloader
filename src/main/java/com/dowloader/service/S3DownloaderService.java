package com.dowloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;

@Service
public class S3DownloaderService {

    private static final Logger log = LoggerFactory.getLogger(S3DownloaderService.class);
    private final S3Client s3;

    @Value("${aws.bucket}")
    private String bucket;

    @Value("${aws.download-dir}")
    private String downloadDir;

    public S3DownloaderService(@Value("${aws.region}") String region) {
        this.s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void downloadRecursively(String prefix) {
        log.info("Starting recursive download from bucket '{}' with prefix '{}'", bucket, prefix);

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response;
        do {
            response = s3.listObjectsV2(listRequest);
            for (S3Object obj : response.contents()) {
                handleObject(obj);
            }
            listRequest = listRequest.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
        } while (response.isTruncated());

        log.info("Download process completed for prefix '{}'", prefix);
    }

    private void handleObject(S3Object obj) {
        String key = obj.key();
        HeadObjectResponse head = s3.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());

        String storageClass = head.storageClassAsString();
        log.info("Found: {} ({})", key, storageClass);

        if ("DEEP_ARCHIVE".equals(storageClass) || "GLACIER".equals(storageClass)) {
            if (head.restore() == null || !head.restore().contains("ongoing-request=\"false\"")) {
                log.warn("Starting restore for '{}'", key);
                s3.restoreObject(RestoreObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .restoreRequest(RestoreRequest.builder()
                                .days(2)
                                .glacierJobParameters(GlacierJobParameters.builder()
                                        .tier(Tier.STANDARD)
                                        .build())
                                .build())
                        .build());
                return;
            } else {
                log.info("File '{}' already restored, starting download", key);
            }
        }
        downloadFile(key);
    }

    private void downloadFile(String key) {
        String localPath = Paths.get(downloadDir, key).toString();
        new File(localPath).getParentFile().mkdirs();
        try (InputStream in = s3.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
             FileOutputStream out = new FileOutputStream(localPath)) {

            in.transferTo(out);
            log.info("Downloaded: {}", key);
        } catch (Exception e) {
            log.error("Error downloading '{}': {}", key, e.getMessage());
        }
    }
}