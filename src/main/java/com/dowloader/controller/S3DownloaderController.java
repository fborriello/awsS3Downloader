package com.dowloader.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dowloader.service.S3DownloaderService;

@RestController
@RequestMapping("/download")
public class S3DownloaderController {

    private static final Logger log = LoggerFactory.getLogger(S3DownloaderController.class);

    private final S3DownloaderService service;

    public S3DownloaderController(final S3DownloaderService service) {
        this.service = service;
    }

    @PostMapping
    public String startDownload(@RequestParam(name = "folderToDownload", required = false) final String folderToDownload) {
        String outcomeMessage;
        if (!StringUtils.hasText(folderToDownload)) {
            log.warn("Download request received without a prefix");
            outcomeMessage = "Error: folderToDownload parameter is required. Example: /download?prefix=backup/xyz/";
        } else {
            log.info("Received download request for prefix '{}'", folderToDownload);

            try {
                service.downloadRecursively(folderToDownload);
                log.info("Download process started for prefix '{}'", folderToDownload);
                outcomeMessage = "Download process started for prefix: " + folderToDownload;
            } catch (Exception e) {
                log.error("Error during download for prefix '{}': {}", folderToDownload, e.getMessage());
                outcomeMessage = "Error starting download for prefix: " + folderToDownload + " (" + e.getMessage() + ")";
            }
        }
        return outcomeMessage;
    }
}
