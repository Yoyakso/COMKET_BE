package com.yoyakso.comket.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/oauth2")
@CrossOrigin("*") // 테스트 환경
public class OAuth2Controller {

    @Value("${google_oauth2_client_id}")
    private String googleClientId;
    @Value("${google_oauth2_client_secret}")
    private String googleClientSecret;

    @Operation(method = "POST", description = "구글 로그인 요청 메서드")
    @PostMapping("/google")
    public String requestGoogleLogin() {
        String requestURL = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId
                + "&redirect_uri=http://localhost:8080/api/v1/oauth2/google&response_type=code&scope=email%20profile%20openid&access_type=offline";
        return requestURL;
    }
}