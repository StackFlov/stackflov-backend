package com.stackflov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stackflov.dto.MessageDto;
import com.stackflov.dto.SmsRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Value("${ncloud.sms.access-key}")
    private String accessKey;
    @Value("${ncloud.sms.secret-key}")
    private String secretKey;
    @Value("${ncloud.sms.service-id}")
    private String serviceId;
    @Value("${ncloud.sms.sender-phone}")
    private String senderPhone;

    // 6자리 인증번호 생성
    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    // NAVER SENS API 시그니처 생성
    private String makeSignature(long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + this.serviceId + "/messages";
        String message = method + space + url + newLine + timestamp + newLine + this.accessKey;
        SecretKeySpec signingKey = new SecretKeySpec(this.secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    // 인증번호 발송
    public void sendVerificationCode(String phoneNumber) {
        String verificationCode = generateVerificationCode();
        long timestamp = System.currentTimeMillis();

        // Redis에 3분간 저장
        redisService.save("SMS_VERIFY:" + phoneNumber, verificationCode, TimeUnit.MINUTES.toMillis(3));

        // SMS Body 구성
        List<MessageDto> messages = new ArrayList<>();
        messages.add(new MessageDto(phoneNumber));
        SmsRequestDto request = new SmsRequestDto("SMS", "COMM", "82", this.senderPhone,
                "[StackFlov] 인증번호 [" + verificationCode + "]를 입력해주세요.", messages);

        try {
            String signature = makeSignature(timestamp);

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://sens.apigw.ntruss.com")
                    .defaultHeader("x-ncp-apigw-timestamp", String.valueOf(timestamp))
                    .defaultHeader("x-ncp-iam-access-key", this.accessKey)
                    .defaultHeader("x-ncp-apigw-signature-v2", signature)
                    .exchangeStrategies(ExchangeStrategies.builder()
                            .codecs(configurer -> configurer
                                    .defaultCodecs()
                                    .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)))
                            .build())
                    .build();

            webClient.post()
                    .uri("/sms/v2/services/" + this.serviceId + "/messages")
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("SENS API Response: {}", response))
                    .doOnError(error -> log.error("SENS API Error: ", error))
                    .subscribe(); // 비동기 실행

        } catch (Exception e) {
            log.error("SMS 발송 중 예외 발생", e);
            throw new RuntimeException("SMS 발송에 실패했습니다.", e);
        }
    }

    // 인증번호 검증
    public boolean verifyCode(String phoneNumber, String inputCode) {
        String savedCode = redisService.get("SMS_VERIFY:" + phoneNumber);
        return savedCode != null && savedCode.equals(inputCode);
    }
}