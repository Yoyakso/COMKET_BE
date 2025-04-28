package com.yoyakso.comket.oauth2.controller;

import com.yoyakso.comket.oauth2.dto.GoogleDetailResponse;
import com.yoyakso.comket.oauth2.dto.GoogleTokenResponse;
import com.yoyakso.comket.oauth2.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/oauth2")
public class OAuth2Controller {
    private final OAuth2Service oAuth2Service;

    @Operation(method = "GET", description = "구글 로그인 페이지 URL 반환 메서드")
    @GetMapping("/google")
    public ResponseEntity<String> requestGoogleLogin() {
        return ResponseEntity.ok(oAuth2Service.returnGoogleLoginPageUrl());
    }

    @GetMapping("/google/login")
    public ResponseEntity<GoogleDetailResponse> googleLoginCallback(@RequestParam String code) {
        GoogleDetailResponse userInfo = oAuth2Service.getGoogleUserInfo(code);
        return ResponseEntity.ok(userInfo);
    }
}