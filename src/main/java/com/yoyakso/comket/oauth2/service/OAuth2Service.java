package com.yoyakso.comket.oauth2.service;

import com.yoyakso.comket.oauth2.dto.GoogleDetailResponse;
import org.springframework.http.ResponseEntity;

public interface OAuth2Service {

    // Google 로그인
    String returnGoogleLoginPageUrl();
    GoogleDetailResponse getGoogleUserInfo(String code);

    // 다른 소셜 로그인 연결
}
