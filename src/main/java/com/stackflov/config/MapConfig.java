package com.stackflov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MapConfig {
    @Value("${kakao.javascript.key}") // application.yml에서 키를 읽어옴
    private String kakaoJsKey;

    @GetMapping("/map/keys/kakao")
    public ResponseEntity<String> getKakaoJsKey() {
        return ResponseEntity.ok(kakaoJsKey);
    }
}