package com.yoyakso.comket.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenRequest {
	private String code;
	private String clientId;
	private String clientSecret;
	private String redirectUri;
}
