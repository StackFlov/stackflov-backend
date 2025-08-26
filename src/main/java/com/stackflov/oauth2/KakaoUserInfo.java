package com.stackflov.oauth2;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // 이메일 동의 항목이 비활성화되었거나 사용자가 동의하지 않은 경우
        if (kakaoAccount == null || kakaoAccount.get("email") == null) {
            // 이메일이 없으면 카카오 ID를 기반으로 임시 이메일을 생성합니다.
            return getProviderId() + "@kakao.com";
        }

        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getName() {
        return (String) profile.get("nickname");
    }
}