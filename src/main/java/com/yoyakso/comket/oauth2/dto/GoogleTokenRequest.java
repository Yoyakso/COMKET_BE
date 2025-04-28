package com.yoyakso.comket.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@AllArgsConstructor
public class GoogleTokenRequest {
    private String code;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
