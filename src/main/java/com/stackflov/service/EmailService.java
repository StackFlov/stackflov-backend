package com.stackflov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisService redisService;

    public void sendVerificationCode(String email) {
        String code = generateCode();
        redisService.save("EMAIL_VERIFY:" + email, code, 5 * 60 * 1000); // 5분 TTL

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[StackFlov] 이메일 인증 코드입니다.");
        message.setText("아래 인증 코드를 입력해주세요:\n\n" + code);

        mailSender.send(message);
    }

    private String generateCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = redisService.get("EMAIL_VERIFY:" + email);
        boolean success = savedCode != null && savedCode.equals(inputCode);

        if (success) {
            // 이메일 인증 성공 → 인증 상태 저장 (10분 TTL)
            redisService.save("EMAIL_VERIFIED:" + email, "true", 10 * 60 * 1000);
        }

        return success;
    }
}
