package com.yoyakso.comket.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.net.HttpHeaders;
import com.yoyakso.comket.auth.service.RefreshTokenService;
import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.member.dto.MemberInfoResponse;
import com.yoyakso.comket.member.dto.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.MemberRegisterResponse;
import com.yoyakso.comket.member.dto.MemberUpdateRequest;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;

	//회원가입
	@PostMapping("/members/register")
	@Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
	public ResponseEntity<MemberRegisterResponse> registerMember(
		@Valid @RequestBody MemberRegisterRequest memberRegisterRequest
	) {
		MemberRegisterResponse response = memberService.registerMember(memberRegisterRequest);

		Member member = memberService.getMemberByEmail(response.getEmail());

		return ResponseEntity
			.ok()
			.header(HttpHeaders.SET_COOKIE, refreshTokenService.getRefreshTokenCookie(member).toString())
			.body(response);
	}

	//회원 정보 조회
	@GetMapping("/members/me")
	@Operation(summary = "회원 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
	public ResponseEntity<MemberInfoResponse> getMember() {
		Member member = memberService.getAuthenticatedMember();
		MemberInfoResponse memberInfoResponse = memberService.buildMemberInfoResponse(member);
		return ResponseEntity.ok(memberInfoResponse);
	}

	//회원 정보 수정
	@PatchMapping("/members/me")
	@Operation(summary = "회원 정보 수정", description = "현재 로그인한 회원의 정보를 수정합니다.")
	public ResponseEntity<MemberInfoResponse> updateMember(
		@Valid @RequestBody MemberUpdateRequest updateRequest
	) {
		Member member = memberService.getAuthenticatedMember();
		Member updatedMember = memberService.updateMember(member, updateRequest);
		MemberInfoResponse memberInfoResponse = memberService.buildMemberInfoResponse(updatedMember);
		return ResponseEntity.ok(memberInfoResponse);
	}

	// 회원 탈퇴
	@DeleteMapping("/members/me")
	@Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원의 계정을 삭제합니다.")
	public ResponseEntity<Void> deleteMember() {
		String email = jwtTokenProvider.getEmail();
		memberService.deleteMember(email);
		return ResponseEntity.noContent().build();
	}
}
