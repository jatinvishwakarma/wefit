package com.wefit.mediaService.controller;

import com.wefit.mediaService.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/upload-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String contentType,
            @RequestParam String userId) {
        String url = mediaService.generatePresignedUploadUrl(contentType, userId);
        return ResponseEntity.ok(Map.of("uploadUrl", url));
    }
}
