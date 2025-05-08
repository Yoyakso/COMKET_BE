package com.yoyakso.comket.oauth2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.oauth2.dto.GoogleLoginResponse;
import com.yoyakso.comket.oauth2.service.OAuth2Service;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/oauth2")
@CrossOrigin("http://localhost:3333")
public class OAuth2Controller {
	private final OAuth2Service oAuth2Service;

	@Operation(method = "GET", description = "구글 OAuth2 로그인 처리")
	@GetMapping("/google/login")
	public ResponseEntity<GoogleLoginResponse> googleLoginCallback(
		@RequestParam(value = "code") String code,
		@RequestParam(value = "redirect") String redirectUri
	) {
		GoogleLoginResponse response = oAuth2Service.handleGoogleLogin(code, redirectUri);
		return ResponseEntity.ok(response);
	}
}