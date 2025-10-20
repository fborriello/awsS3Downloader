package com.dowloader.controller;

import com.dowloader.service.S3DownloaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/download")
public class S3DownloaderController {

    private static final Logger log = LoggerFactory.getLogger(S3DownloaderController.class);

    private final S3DownloaderService service;

    public S3DownloaderController(S3DownloaderService service) {
        this.service = service;
    }

    @PostMapping
    public String startDownload(@RequestParam(name = "prefix", required = false) String prefix) {
        String outcomeMessage;
        if (prefix == null || prefix.isBlank()) {
            log.warn("Download request received without a prefix");
            outcomeMessage = "Error: prefix parameter is required. Example: /download?prefix=backup/2024/";
        } else {
            log.info("Received download request for prefix '{}'", prefix);

            try {
                service.downloadRecursively(prefix);
                log.info("Download process started for prefix '{}'", prefix);
                outcomeMessage = "Download process started for prefix: " + prefix;
            } catch (Exception e) {
                log.error("Error during download for prefix '{}': {}", prefix, e.getMessage());
                outcomeMessage = "Error starting download for prefix: " + prefix + " (" + e.getMessage() + ")";
            }
        }
        return outcomeMessage;
    }
}