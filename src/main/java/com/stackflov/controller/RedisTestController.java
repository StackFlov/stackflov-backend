package com.stackflov.controller;

import com.stackflov.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/redis")
public class RedisTestController {

    private final RedisService redisService;

    @PostMapping("/save")
    public String save(@RequestParam String key, @RequestParam String value) {
        redisService.save(key, value);
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
