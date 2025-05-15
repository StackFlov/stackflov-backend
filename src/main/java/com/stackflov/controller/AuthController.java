package com.stackflov.controller;

import com.stackflov.domain.User;
import com.stackflov.dto.TokenResponseDto;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.repository.UserRepository;
import com.stackflov.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final UserRepository userRepository;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody ReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }

        String email = jwtProvider.getEmail(refreshToken);
        String savedRefreshToken = redisService.get("RT:" + email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.createAccessToken(email, user.getRole().name());
        String newRefreshToken = jwtProvider.createRefreshToken(email);

        redisService.save("RT:" + email, newRefreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, newRefreshToken));
    }
}

// DTO for refresh token request
@Getter
class ReissueRequest {
    private String refreshToken;
}
