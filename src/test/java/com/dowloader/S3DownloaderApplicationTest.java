package com.dowloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class S3DownloaderApplicationTest {

    /**
     * Test that verifies the Spring Boot application starts successfully
     * without throwing any exceptions during initialization.
     */
    @Test
    void mainRunsWithoutException() {
        assertDoesNotThrow(() -> S3DownloaderApplication.main(new String[]{}));
    }
}

