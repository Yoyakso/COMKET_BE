package com.yoyakso.comket.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.auth.dto.GoogleDetailResponse;
import com.yoyakso.comket.auth.dto.GoogleLoginResponse;
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

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email);
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
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
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

		String token = jwtTokenProvider.createToken(member.getEmail());

		return MemberRegisterResponse.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.realName(member.getRealName())
			.department(member.getDepartment())
			.role(member.getRole())
			.responsibility(member.getResponsibility())
			.token(token)
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
		return memberRepository.save(member);
	}

	// 구글 로그인 로직 처리
	public GoogleLoginResponse handleOAuth2Member(GoogleDetailResponse googleUserInfo) {
		Member member = memberRepository.findByEmail(googleUserInfo.getEmail());

		if (member == null) {
			return new GoogleLoginResponse(null, null, googleUserInfo.getEmail());
		}

		String token = jwtTokenProvider.createToken(member.getEmail());

		return new GoogleLoginResponse(token, member.getRealName(), member.getEmail());
	}

	public Member getAuthenticatedMember() {
		String email = jwtTokenProvider.getEmail();
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		return member;
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
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		return member;
	}
}