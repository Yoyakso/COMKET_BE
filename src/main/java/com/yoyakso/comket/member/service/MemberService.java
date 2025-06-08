package com.yoyakso.comket.member.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.auth.dto.GoogleDetailResponse;
import com.yoyakso.comket.auth.dto.LoginResponse;
import com.yoyakso.comket.auth.service.RefreshTokenService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.member.dto.request.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.request.MemberUpdateRequest;
import com.yoyakso.comket.member.dto.response.MemberRegisterResponse;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.mapper.MemberMapper;
import com.yoyakso.comket.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final FileService fileService;
	private final RefreshTokenService refreshTokenService;
	private final MemberMapper memberMapper;

	public MemberRegisterResponse registerMember(MemberRegisterRequest memberRegisterRequest) {
		// 삭제되지 않은 회원 중 동일 이메일이 있는지 확인
		if (memberRepository.findByEmailAndIsDeletedFalse(memberRegisterRequest.getEmail()).isPresent()) {
			throw new CustomException("EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다.");
		}

		// 삭제된 회원인 경우 재활성화
		Optional<Member> deletedMember = memberRepository.findByEmailAndIsDeletedTrue(memberRegisterRequest.getEmail());
		if (deletedMember.isPresent()) {
			Member member = deletedMember.get();
			member.setIsDeleted(false);
			member.setPassword(passwordEncoder.encode(memberRegisterRequest.getPassword()));
			// 필요한 정보 업데이트
			member.setFullName(memberRegisterRequest.getFullName());
			memberRepository.save(member);

			// JWT 토큰 생성
			String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
			return memberMapper.toMemberRegisterResponse(member, accessToken);
		}

		// 신규 회원 등록 (기존 로직)
		Member member = memberMapper.toEntity(memberRegisterRequest);
		member.setPassword(passwordEncoder.encode(member.getPassword()));
		memberRepository.save(member);

		String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
		return memberMapper.toMemberRegisterResponse(member, accessToken);
	}

	public Member updateMember(Member member, MemberUpdateRequest updateRequest) {
		memberMapper.updateMemberFromRequest(member, updateRequest);
		return memberRepository.save(member);
	}

	public void deleteMember(Member member) {
		// RefreshToken 삭제
		refreshTokenService.deleteRefreshToken(member.getId().toString());
		// 회원 정보 삭제 && soft delete 처리
		member.setIsDeleted(true);
		memberRepository.save(member);
	}
	//-------------

	public Member findByEmail(String email) {
		return getMemberByEmail(email);
	}

	public String findMemberNameById(Long id) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."));

		return member.getFullName();
	}

	// 구글 로그인 로직 처리
	public LoginResponse handleOAuth2Member(GoogleDetailResponse googleUserInfo) {
		Optional<Member> memberOptional = memberRepository.findByEmail(googleUserInfo.getEmail());

		if (memberOptional.isEmpty()) {
			return LoginResponse.builder()
				.memberId(null)
				.name(null)
				.email(googleUserInfo.getEmail())
				.accessToken(null)
				.loginPlatformInfo(null)
				.build();
		}

		Member member = memberOptional.get();

		// 탈퇴한 회원 검증 추가
		if (member.getIsDeleted()) {
			return LoginResponse.builder()
				.memberId(null)
				.name(null)
				.email(googleUserInfo.getEmail())
				.accessToken(null)
				.loginPlatformInfo(null)
				.isDeleted(true) // 탈퇴 상태 표시
				.build();
		}

		String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());

		return LoginResponse.builder()
			.memberId(member.getId())
			.name(member.getFullName())
			.email(member.getEmail())
			.accessToken(accessToken)
			.loginPlatformInfo("GOOGLE")
			.build();
	}

	public Member getAuthenticatedMember() {
		String email = jwtTokenProvider.getEmail();
		return getMemberByEmail(email);
	}

	public Member getMemberById(Long targetMemberId) {
		Member member = memberRepository.findById(targetMemberId)
			.orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."));

		// 탈퇴한 회원 검증 추가
		if (member.getIsDeleted()) {
			throw new CustomException("MEMBER_DELETED", "탈퇴한 회원입니다.");
		}

		return member;
	}

	public Member getMemberByEmail(String email) {
		Optional<Member> member = memberRepository.findByEmail(email);
		if (member.isEmpty()) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}

		// 탈퇴한 회원 검증 추가
		if (member.get().getIsDeleted()) {
			throw new CustomException("MEMBER_DELETED", "탈퇴한 회원입니다.");
		}

		return member.get();
	}

	public Optional<Member> getMemberByEmailOptional(String email) {
		return memberRepository.findByEmail(email);
	}

	// 비밀번호 업데이트
	public void updatePassword(String email, String newPassword) {
		Member member = getMemberByEmail(email);
		member.setPassword(passwordEncoder.encode(newPassword));
		memberRepository.save(member);
	}
}
