package com.stackflov.oauth2;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo;

        if (registrationId.equals("google")) {
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else if (registrationId.equals("naver")) { // 👈 이 부분을 추가합니다.
            oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
        } else {
            oAuth2UserInfo = null;
        }
        User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .orElseGet(() -> registerNewUser(oAuth2UserInfo, registrationId));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo userInfo, String registrationId) {
        String profileImage = null;
        // 네이버 사용자 정보일 경우에만 프로필 이미지 가져오기
        if (userInfo instanceof NaverUserInfo) {
            profileImage = ((NaverUserInfo) userInfo).getProfileImage();
        }
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getName())
                .profileImage(profileImage)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 소셜 로그인은 비밀번호가 무의미
                .role(Role.USER)
                .socialType(SocialType.valueOf(registrationId.toUpperCase()))
                .socialId(userInfo.getProviderId())
                .active(true)
                .build();
        return userRepository.save(newUser);
    }
}