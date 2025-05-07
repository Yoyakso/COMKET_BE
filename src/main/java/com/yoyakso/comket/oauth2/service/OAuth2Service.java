package com.yoyakso.comket.oauth2.service;

import com.yoyakso.comket.oauth2.dto.GoogleLoginResponse;

public interface OAuth2Service {

	// Google 로그인
	String returnGoogleLoginPageUrl();

	GoogleLoginResponse handleGoogleLogin(String code, String redirectUri);

	// 다른 소셜 로그인 연결
}
