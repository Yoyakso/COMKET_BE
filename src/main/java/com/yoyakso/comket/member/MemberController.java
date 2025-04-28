package com.yoyakso.comket.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.member.dto.MemberInfoResponse;
import com.yoyakso.comket.member.dto.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.MemberRegisterResponse;
import com.yoyakso.comket.member.dto.MemberUpdateRequest;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.util.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class MemberController {
	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
		this.memberService = memberService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	//회원가입
	@PostMapping("/auth/register")
	public ResponseEntity<MemberRegisterResponse> registerMember(
		@RequestBody MemberRegisterRequest memberRegisterRequest) {
		Member member = Member.fromRequest(memberRegisterRequest);
		MemberRegisterResponse response = memberService.registerMember(member);
		return ResponseEntity.ok(response);

	}

	//회원 정보 조회
	@GetMapping("/members/me")
	public ResponseEntity<MemberInfoResponse> getMember(HttpServletRequest request) {

		Member member = getAuthenticatedMember(request);
		if (member == null) {
			return ResponseEntity.notFound().build(); // 회원 정보가 없을 경우
		}

		MemberInfoResponse response = new MemberInfoResponse(
			member.getEmail(),
			member.getNickname(),
			member.getRealName()
		);
		return ResponseEntity.ok(response);
	}

	//회원 정보 수정
	@PatchMapping("/members/me")
	public ResponseEntity<MemberInfoResponse> updateMember(HttpServletRequest request,
		@RequestBody MemberUpdateRequest updateRequest) {
		Member member = getAuthenticatedMember(request);
		if (member == null) {
			return ResponseEntity.notFound().build(); // 회원 정보가 없을 경우
		}

		Member updatedMember = memberService.updateMember(member, updateRequest);

		MemberInfoResponse response = new MemberInfoResponse(
			updatedMember.getEmail(),
			updatedMember.getNickname(),
			updatedMember.getRealName()
		);
		return ResponseEntity.ok(response);
	}

	// 회원 탈퇴
	@DeleteMapping("/members/me")
	public ResponseEntity<Void> deleteMember(HttpServletRequest request) {
		String token = jwtTokenProvider.getTokenFromHeader(request);
		if (token == null) {
			return ResponseEntity.badRequest().build(); // 토큰이 없을 경우
		}

		String email = jwtTokenProvider.parseToken(token).getSubject();
		memberService.deleteMember(email);
		return ResponseEntity.noContent().build();
	}

	private Member getAuthenticatedMember(HttpServletRequest request) {
		String token = jwtTokenProvider.getTokenFromHeader(request);
		if (token == null) {
			return null; // 토큰이 없을 경우
		}

		String email = jwtTokenProvider.parseToken(token).getSubject();
		return memberService.findByEmail(email);
	}
}
