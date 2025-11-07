package com.dowloader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.RestoreObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

class S3DownloaderServiceTest {

    private S3Client s3Client;
    private S3DownloaderService service;

    /**
     * Set up test environment before each test.
     * Creates a mock S3Client and initializes the service with test configuration.
     */
    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        service = new S3DownloaderService();
        ReflectionTestUtils.setField(service, "s3", s3Client);
        ReflectionTestUtils.setField(service, "bucket", "test-bucket");
        ReflectionTestUtils.setField(service, "downloadDir", "target/downloads");
    }

    /**
     * Test that verifies the service correctly downloads all objects with STANDARD storage class.
     * This test ensures that:
     * - Objects are listed from S3
     * - Object metadata is checked via headObject
     * - Objects are downloaded via getObject
     */
    @Test
    void downloadRecursivelyDownloadsAllObjects() {
        // Given: two S3 objects with STANDARD storage class
        S3Object obj1 = S3Object.builder().key("prefix/file1.txt").build();
        S3Object obj2 = S3Object.builder().key("prefix/file2.txt").build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(obj1, obj2)
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        HeadObjectResponse head = HeadObjectResponse.builder().storageClass("STANDARD").build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

        ResponseInputStream<GetObjectResponse> mockResponse = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream("test content".getBytes()))
        );
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponse);

        // When: download is initiated
        service.downloadRecursively("prefix/");

        // Then: verify all S3 operations are called the expected number of times
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(2)).headObject(any(HeadObjectRequest.class));
        verify(s3Client, times(2)).getObject(any(GetObjectRequest.class));
    }

    /**
     * Test that verifies the service initiates a restore request for objects in GLACIER storage class.
     * Objects in GLACIER or DEEP_ARCHIVE storage classes must be restored before they can be downloaded.
     * This test ensures that:
     * - Glacier objects trigger a restore request
     * - Download is not attempted until restore completes
     */
    @Test
    void handleObjectWithGlacierStorageClassStartsRestore() {
        // Given: an S3 object with GLACIER storage class that needs restoration
        S3Object obj = S3Object.builder().key("prefix/file3.txt").build();

        HeadObjectResponse head = HeadObjectResponse.builder()
                .storageClass("GLACIER")
                .restore("ongoing-request=\"true\"")
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(head);

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(obj)
                .isTruncated(false)
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        // When: download is initiated for a Glacier object
        service.downloadRecursively("prefix/");

        // Then: restore is requested and download is skipped
        verify(s3Client, times(1)).restoreObject(any(RestoreObjectRequest.class));
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }
}

