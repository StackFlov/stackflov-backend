package com.stackflov.oauth2;

import java.util.Map;

// 제공자(Google, Naver 등)별로 다른 사용자 정보 속성을 하나로 통일하는 인터페이스
public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getName();
}