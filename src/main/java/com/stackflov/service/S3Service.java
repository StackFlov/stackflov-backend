package com.stackflov.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3 업로드 (폴더 dirName 하위에 저장)
     * @param file   업로드할 파일
     * @param dirName S3 폴더명 (예: "images" / "profiles" 등). null 또는 빈값이면 루트에 저장
     * @return 업로드된 파일의 퍼블릭 URL
     */
    public String upload(MultipartFile file, String dirName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
        }

        String key = buildKey(dirName, safeFileName(file.getOriginalFilename()));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            metadata.setContentType(file.getContentType());
        }

        try (InputStream is = file.getInputStream()) {
            PutObjectRequest req = new PutObjectRequest(bucket, key, is, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            amazonS3.putObject(req);
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }

        URL url = amazonS3.getUrl(bucket, key);
        return url.toString();
    }

    /**
     * (선택) URL로 삭제 – 실제 파일 삭제가 필요할 때만 사용
     * 소프트 삭제(논리 삭제)만 할 거면 이 메서드는 호출하지 말고 DB 플래그만 내려줘.
     */
    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        String key = extractKeyFromUrl(fileUrl);
        amazonS3.deleteObject(bucket, key);
    }

    /* ---------- private helpers ---------- */

    private String buildKey(String dirName, String filename) {
        if (dirName == null || dirName.isBlank()) return filename;
        // 중복 방지를 위해 UUID prefix
        return dirName.replaceAll("^/+", "").replaceAll("/+$", "") + "/" + filename;
    }

    private String safeFileName(String original) {
        String base = (original == null || original.isBlank()) ? "file" : original.trim();
        // 공백/특수문자 제거 + UUID prefix
        String cleaned = base.replaceAll("[^A-Za-z0-9._-]", "_");
        return UUID.randomUUID() + "_" + cleaned;
    }

    private String extractKeyFromUrl(String fileUrl) {
        // fileUrl 예: https://{bucket}.s3.{region}.amazonaws.com/dir/uuid_name.png
        // 혹은 virtual-hosted-style 아닌 path-style일 수도 있으니, 호스트 부분 제거 후 path만 key로 사용
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath(); // "/dir/uuid_name.png"
            return path.replaceFirst("^/", "");
        } catch (Exception e) {
            // URL 파싱 실패 시 그냥 마지막 "/" 뒤를 키로 가정 (폴더 포함 가능)
            int idx = fileUrl.indexOf(".amazonaws.com/");
            if (idx > 0) {
                return fileUrl.substring(idx + ".amazonaws.com/".length());
            }
            // 최후의 수단
            int last = fileUrl.lastIndexOf('/');
            return last >= 0 ? fileUrl.substring(last + 1) : fileUrl;
        }
    }
}
