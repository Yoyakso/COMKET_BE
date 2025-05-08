package com.yoyakso.comket.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.auth.dto.GoogleLoginResponse;
import com.yoyakso.comket.auth.dto.LoginRequest;
import com.yoyakso.comket.auth.dto.LoginResponse;
import com.yoyakso.comket.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("http://localhost:3333")
public class AuthController {
	private final AuthService authService;

	@Operation(method = "GET", description = "구글 OAuth2 로그인 처리")
	@GetMapping("/oauth2/google/login")
	public ResponseEntity<GoogleLoginResponse> googleLoginCallback(
		@RequestParam(value = "code") String code,
		@RequestParam(value = "redirect") String redirectUri
	) {
		GoogleLoginResponse response = authService.handleGoogleLogin(code, redirectUri);
		return ResponseEntity.ok(response);
	}

	@Operation(method = "POST", description = "자체 로그인 API")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> requestLogin(@RequestBody LoginRequest requestInfo) {
		LoginResponse response = authService.login(requestInfo);
		return ResponseEntity.ok(response);
	}
}