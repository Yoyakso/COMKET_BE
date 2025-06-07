package com.yoyakso.comket.email.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.email.dto.EmailVerificationCodeSendRequest;
import com.yoyakso.comket.email.dto.EmailVerificationCodeVerifyRequest;
import com.yoyakso.comket.email.dto.PasswordResetLinkRequest;
import com.yoyakso.comket.email.dto.PasswordResetRequest;
import com.yoyakso.comket.email.service.EmailService;
import com.yoyakso.comket.member.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
public class EmailController {
	private final EmailService emailService;
	private final MemberService memberService;

	// 인증 번호 전송
	@Operation(summary = "이메일 인증 번호 전송 API", description = "이메일로 인증 번호를 전송하는 API")
	@PostMapping("/verify/send")
	public ResponseEntity<Void> sendVerificationCode(
		@RequestBody EmailVerificationCodeSendRequest sendRequest) {
		emailService.createVerificationCode(sendRequest.getEmail());
		return ResponseEntity.ok().build();
	}

	// 인증 번호 확인
	@Operation(summary = "이메일 인증 번호 확인 API", description = "이메일로 전송된 인증 번호를 확인하는 API")
	@PostMapping("/verify/code")
	public ResponseEntity<Void> verifyVerificationCode(
		@RequestBody EmailVerificationCodeVerifyRequest verifyRequest) {
		emailService.verifyVerificationCode(verifyRequest.getEmail(), verifyRequest.getCode());
		return ResponseEntity.ok().build();
	}

	// 비밀번호 재설정 링크 전송
	@Operation(summary = "비밀번호 재설정 링크 전송 API", description = "비밀번호 재설정 링크를 이메일로 전송하는 API")
	@PostMapping("/password-reset/send")
	public ResponseEntity<Void> sendPasswordResetLink(
		@RequestBody PasswordResetLinkRequest resetLinkRequest) {
		emailService.sendPasswordResetLink(resetLinkRequest.getEmail());
		return ResponseEntity.ok().build();
	}

	// 비밀번호 재설정
	@Operation(summary = "비밀번호 재설정 API", description = "새 비밀번호로 재설정하는 API")
	@PostMapping("/password-reset")
	public ResponseEntity<Void> resetPassword(
		@RequestBody PasswordResetRequest resetRequest) {
		emailService.resetPassword(resetRequest.getEmail(), resetRequest.getCode(), resetRequest.getNewPassword());
		return ResponseEntity.ok().build();
	}
}
