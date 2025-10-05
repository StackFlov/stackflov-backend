package com.stackflov.controller;

import com.stackflov.service.RedisService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class RedisTestController {

    private final RedisService redisService;

    @PostMapping("/save")
    public String save(@RequestParam String key, @RequestParam String value) {
        long oneHourMillis = 1000L * 60 * 60;  // 1시간 TTL 설정
        redisService.save(key, value, oneHourMillis);
        return "저장 완료";
    }

    @GetMapping("/get")
    public String get(@RequestParam String key) {
        return redisService.get(key);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam String key) {
        redisService.delete(key);
        System.out.println("http://localhost:8080/swagger-ui/index.html");
        return "삭제 완료";
    }

}
