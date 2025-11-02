package com.stackflov.controller;

import com.stackflov.config.CustomUserPrincipal;
import com.stackflov.dto.*;
import com.stackflov.service.AuthService;
import com.stackflov.service.EmailService;
import com.stackflov.service.SmsService;
import com.stackflov.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;

@Tag(
        name = "Auth",
        description = "회원가입/로그인, 토큰 재발급, 이메일·문자 인증 등 인증 관련 API"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${app.frontend.callback:http://localhost:3000}")
    private String frontendCallback;

    @Value("${app.cookie-samesite:Lax}")  // dev=Lax, prod=None
    private String cookieSameSite;
    @Value("${app.cookie-secure:false}")  // dev=false, prod=true
    private boolean cookieSecure;
    @Value("${app.cookie-domain:}")
    private String cookieDomain;

    @GetMapping("/callback")
    public ResponseEntity<Void> callback() {
        return ResponseEntity.status(HttpStatus.FOUND) // 302
                .location(URI.create(frontendCallback))
                .build();
    }

    private static String readCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    @Operation(summary = "내 정보", description = "현재 로그인한 사용자의 프로필을 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // ✅ 서비스 메서드 이름/리턴타입에 맞게 호출
        return ResponseEntity.ok(userService.getUserByEmail(principal.getEmail()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest req, HttpServletResponse res) {
        String refresh = readCookie(req, "REFRESH_TOKEN");
        if (refresh == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // ✅ AuthService의 reissueToken을 사용 (access + refresh 둘 다 발급)
        TokenResponseDto t = authService.reissueToken(refresh);

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", t.getAccessToken())
                .httpOnly(true).secure(true).sameSite("None")
                .domain(".stackflov.com").path("/")
                .maxAge(15 * 60).build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", t.getRefreshToken())
                .httpOnly(true).secure(true).sameSite("None")
                .domain(".stackflov.com").path("/")
                .maxAge(14L * 24 * 60 * 60).build();

        res.addHeader("Set-Cookie", accessCookie.toString());
        res.addHeader("Set-Cookie", refreshCookie.toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원가입", description = "이메일/비밀번호 및 기본 정보를 이용해 새 계정을 생성합니다.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequestDto signupRequestDto) {
        userService.register(signupRequestDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            TokenResponseDto tokens = userService.login(loginRequestDto);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "로그아웃", description = "쿠키를 만료시키고 서버측 세션/리프레시를 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal,
                                       HttpServletResponse res) {
        if (principal != null) authService.logout(principal.getEmail()); // Redis RT 제거

        // ✅ 실제/과거 이름 전부 제거
        killCookie(res, "accessToken");
        killCookie(res, "refreshToken");
        killCookie(res, "ACCESS_TOKEN");   // 과거 이름
        killCookie(res, "REFRESH_TOKEN");  // 과거 이름

        return ResponseEntity.noContent().build();
    }

    /** host-only + domain 쿠키 모두 만료해서 안전하게 삭제 */
    private void killCookie(HttpServletResponse res, String name) {
        // 1) host-only 쿠키 삭제
        ResponseCookie c1 = ResponseCookie.from(name, "")
                .path("/")
                .maxAge(0)
                .httpOnly(false)          // 삭제에는 영향 없음
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();
        res.addHeader("Set-Cookie", c1.toString());

        // 2) domain 지정 쿠키 삭제(있을 때만)
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            ResponseCookie c2 = ResponseCookie.from(name, "")
                    .domain(cookieDomain)
                    .path("/")
                    .maxAge(0)
                    .httpOnly(false)
                    .secure(cookieSecure)
                    .sameSite(cookieSameSite)
                    .build();
            res.addHeader("Set-Cookie", c2.toString());
        }
    }

    @Operation(summary = "이메일 인증코드 전송", description = "입력한 이메일로 인증 코드를 발송합니다.")
    @PostMapping("/email/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequestDto requestDto) {
        emailService.sendVerificationCode(requestDto.getEmail());
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    @Operation(summary = "이메일 인증코드 검증", description = "수신한 이메일 인증 코드를 검증합니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailCodeVerifyRequestDto dto) {
        boolean result = emailService.verifyCode(dto.getEmail(), dto.getCode());
        return result
                ? ResponseEntity.ok("이메일 인증 성공")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일 인증 실패");
    }

    @Operation(summary = "휴대폰 인증번호 전송", description = "입력한 휴대폰 번호로 인증번호를 발송합니다.")
    @PostMapping("/phone/send")
    public ResponseEntity<String> sendSmsCode(@RequestBody PhoneRequestDto requestDto) {
        smsService.sendVerificationCode(requestDto.getPhoneNumber());
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @Operation(summary = "휴대폰 인증번호 검증", description = "수신한 휴대폰 인증번호를 검증합니다.")
    @PostMapping("/phone/verify")
    public ResponseEntity<String> verifySmsCode(@RequestBody PhoneVerifyRequestDto requestDto) {
        boolean isVerified = smsService.verifyCode(requestDto.getPhoneNumber(), requestDto.getCode());
        if (isVerified) {
            return ResponseEntity.ok("전화번호 인증에 성공했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 일치하지 않습니다.");
        }
    }

}

@Getter
class ReissueRequest {
    private String refreshToken;
}
