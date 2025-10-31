package com.dowloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S3DownloaderApplication {
    public static void main(final String[] args) {
        SpringApplication.run(S3DownloaderApplication.class, args);
    }
}