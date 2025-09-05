package com.stackflov.controller;

import com.stackflov.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile multipartFile) throws IOException {
        String imageUrl = s3Service.upload(multipartFile, "images"); // "images"는 S3 버킷 안의 폴더 이름
        return ResponseEntity.ok(imageUrl);
    }
}