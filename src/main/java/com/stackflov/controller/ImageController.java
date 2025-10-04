package com.stackflov.controller;

import com.stackflov.service.S3Service;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Hidden
@RestController
@RequiredArgsConstructor
public class ImageController {
    private final S3Service s3Service;

    @PreAuthorize("hasRole('ADMIN')") // 최소 관리자만
    @PostMapping(value="/images/upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile f) throws IOException {
        return ResponseEntity.ok(s3Service.upload(f, "images"));
    }
}