package com.dowloader.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import com.dowloader.service.S3DownloaderService;

class S3DownloaderControllerTest {

    /**
     * Test that verifies the controller returns an error message when the prefix parameter is null.
     * The service should not be invoked in this case.
     */
    @Test
    void startDownloadWithNullPrefixReturnsError() {
        // Given: a mocked service and controller
        S3DownloaderService service = mock(S3DownloaderService.class);
        S3DownloaderController controller = new S3DownloaderController(service);

        // When: startDownload is called with null prefix
        String result = controller.startDownload(null);

        // Then: an error message is returned and service is not called
        assertTrue(result.contains("Error: prefix parameter is required"));
        verifyNoInteractions(service);
    }

    /**
     * Test that verifies the controller returns an error message when the prefix parameter is blank.
     * The service should not be invoked in this case.
     */
    @Test
    void startDownloadWithBlankPrefixReturnsError() {
        // Given: a mocked service and controller
        S3DownloaderService service = mock(S3DownloaderService.class);
        S3DownloaderController controller = new S3DownloaderController(service);

        // When: startDownload is called with blank prefix
        String result = controller.startDownload("   ");

        // Then: an error message is returned and service is not called
        assertTrue(result.contains("Error: prefix parameter is required"));
        verifyNoInteractions(service);
    }

    /**
     * Test that verifies the controller successfully initiates a download process
     * when a valid prefix is provided and delegates to the service.
     */
    @Test
    void startDownloadWithValidPrefixStartsDownload() {
        // Given: a mocked service and controller
        S3DownloaderService service = mock(S3DownloaderService.class);
        S3DownloaderController controller = new S3DownloaderController(service);

        // When: startDownload is called with a valid prefix
        String prefix = "backup/xyz/";
        String result = controller.startDownload(prefix);

        // Then: a success message is returned and service is called once
        assertTrue(result.contains("Download process started for prefix"));
        verify(service, times(1)).downloadRecursively(prefix);
    }

    /**
     * Test that verifies the controller handles exceptions from the service gracefully
     * and returns an error message containing the exception details.
     */
    @Test
    void startDownloadWithExceptionReturnsError() {
        // Given: a mocked service that throws an exception
        S3DownloaderService service = mock(S3DownloaderService.class);
        doThrow(new RuntimeException("fail")).when(service).downloadRecursively(anyString());
        S3DownloaderController controller = new S3DownloaderController(service);

        // When: startDownload is called
        String result = controller.startDownload("backup/xyz/");

        // Then: an error message with exception details is returned
        assertTrue(result.contains("Error starting download for prefix"));
        assertTrue(result.contains("fail"));
        verify(service, times(1)).downloadRecursively("backup/xyz/");
    }
}

