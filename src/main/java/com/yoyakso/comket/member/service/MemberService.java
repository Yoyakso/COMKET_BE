package com.yoyakso.comket.member.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.auth.dto.GoogleDetailResponse;
import com.yoyakso.comket.auth.dto.LoginResponse;
import com.yoyakso.comket.auth.service.RefreshTokenService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.enums.FileCategory;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.member.dto.MemberInfoResponse;
import com.yoyakso.comket.member.dto.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.MemberRegisterResponse;
import com.yoyakso.comket.member.dto.MemberUpdateRequest;
import com.yoyakso.comket.member.entity.Member;
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

	public Member findByEmail(String email) {
		return getMemberByEmail(email);
	}

	public String findMemberNameById(Long id) {
		Member member = memberRepository.findById(id)
			.orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."));

		return member.getRealName();
	}

	public void save(Member member) {
		if (member.getId() == null) {
			memberRepository.save(member);
		} else {
			Member existingMember = memberRepository.findById(member.getId())
				.orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."));
			existingMember.setRealName(member.getRealName());
			memberRepository.save(existingMember);
		}
	}

	public void deleteMember(String email) {
		Member member = getMemberByEmail(email);
		memberRepository.delete(member);
	}

	public MemberRegisterResponse registerMember(MemberRegisterRequest memberRegisterRequest) {
		if (memberRepository.existsByEmail(memberRegisterRequest.getEmail())) {
			throw new CustomException("EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다.");
		}
		Member member = Member.fromRequest(memberRegisterRequest);

		// profileFileId로 File 엔티티 조회 및 설정
		String profileFileUrl = null;
		if (memberRegisterRequest.getProfileFileId() != null) {
			File profileFile = fileService.getFileById(memberRegisterRequest.getProfileFileId());
			fileService.validateFileCategory(profileFile, FileCategory.MEMBER_PROFILE);
			member.setProfileFile(profileFile);
			profileFileUrl = fileService.getFileUrlByPath(profileFile.getFilePath());
		}

		member.setPassword(passwordEncoder.encode(member.getPassword()));
		memberRepository.save(member);

		String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());

		return MemberRegisterResponse.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.realName(member.getRealName())
			.department(member.getDepartment())
			.role(member.getRole())
			.responsibility(member.getResponsibility())
			.accessToken(accessToken)
			.profileFileUrl(profileFileUrl)
			.build();
	}

	public Member updateMember(Member member, MemberUpdateRequest updateRequest) {
		if (updateRequest.getRealName() != null) {
			member.setRealName(updateRequest.getRealName());
		}
		if (updateRequest.getProfileFileId() != null) {
			File profileFile = fileService.getFileById(updateRequest.getProfileFileId());
			fileService.validateFileCategory(profileFile, FileCategory.MEMBER_PROFILE);
			member.setProfileFile(profileFile);
		}
		member.setDepartment(updateRequest.getDepartment());
		member.setRole(updateRequest.getRole());
		member.setResponsibility(updateRequest.getResponsibility());
		return memberRepository.save(member);
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
				.accessToken(null)
				.loginPlatformInfo(null)
				.build();
		}
		Member member = memberOptional.get();

		String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
		String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

		return LoginResponse.builder()
			.memberId(member.getId())
			.name(member.getRealName())
			.email(member.getEmail())
			.accessToken(accessToken)
			.loginPlatformInfo("GOOGLE")
			.build();
	}

	public Member getAuthenticatedMember() {
		String email = jwtTokenProvider.getEmail();
		return getMemberByEmail(email);
	}

	public MemberInfoResponse buildMemberInfoResponse(Member member) {
		String profileFileUrl = member.getProfileFile() != null
			? fileService.getFileUrlByPath(member.getProfileFile().getFilePath())
			: null;

		return MemberInfoResponse.builder()
			.email(member.getEmail())
			.realName(member.getRealName())
			.department(member.getDepartment())
			.role(member.getRole())
			.responsibility(member.getResponsibility())
			.profileFileUrl(profileFileUrl)
			.build();
	}

	public Member getMemberById(Long targetMemberId) {
		return memberRepository.findById(targetMemberId)
			.orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."));
	}

	public Member getMemberByEmail(String email) {
		Optional<Member> member = memberRepository.findByEmail(email);
		if (member.isEmpty()) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		return member.get();
	}

	public Optional<Member> getMemberByEmailOptional(String email) {
		return memberRepository.findByEmail(email);
	}
}