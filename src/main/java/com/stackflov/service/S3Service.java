package com.stackflov.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

// S3Service.java (교체)
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.cdn.domain:}")   // 없으면 빈값
    private String cdnDomain;

    // 업로드는 key(예: images/uuid.png) 를 리턴하도록 통일
    public String upload(MultipartFile file, String dir) {
        String key = dir + "/" + UUID.randomUUID() + "_" + Objects.requireNonNull(file.getOriginalFilename());
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        try (InputStream in = file.getInputStream()) {
            s3.putObject(bucket, key, in, meta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return key;
    }

    // ✅ URL이든 key든 받아서 key만 뽑아줌 (이 이름으로 사용)
    public String extractKey(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) return urlOrKey;
        // 이미 key 형태면 그대로
        if (!urlOrKey.startsWith("http://") && !urlOrKey.startsWith("https://")) {
            return urlOrKey.replaceFirst("^/+", "");
        }
        try {
            URL u = new URL(urlOrKey);
            return u.getPath().replaceFirst("^/+", "");
        } catch (Exception ignore) {
            int i = urlOrKey.indexOf(".amazonaws.com/");
            if (i > 0) return urlOrKey.substring(i + ".amazonaws.com/".length());
            return urlOrKey.replaceFirst("^/+", "");
        }
    }

    // ✅ key 기반 삭제
    public void deleteByKey(String key) {
        s3.deleteObject(bucket, extractKey(key));
    }

    // ✅ 최종 공개 URL 생성 (CDN 있으면 CDN, 아니면 S3 URL)
    public String publicUrl(String keyOrUrl) {
        String key = extractKey(keyOrUrl);
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            return "https://" + cdnDomain + "/" + key;
        }
        return s3.getUrl(bucket, key).toString();
    }
}

