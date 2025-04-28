package com.yoyakso.comket.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.dto.MemberRegisterResponse;
import com.yoyakso.comket.member.dto.MemberUpdateRequest;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.util.JwtTokenProvider;

@Service
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public void save(Member member) {
		if (member.getId() == null) {
			memberRepository.save(member);
		} else {
			Member existingMember = memberRepository.findById(member.getId())
				.orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."));
			existingMember.setNickname(member.getNickname());
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

	public MemberRegisterResponse registerMember(Member member) {
		if (memberRepository.existsByEmail(member.getEmail())) {
			throw new CustomException("EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다.");
		}

		if (memberRepository.existsByNickname(member.getNickname())) {
			throw new CustomException("NICKNAME_DUPLICATE", "이미 사용 중인 닉네임입니다.");
		}

		member.setPassword(passwordEncoder.encode(member.getPassword()));
		memberRepository.save(member);

		String token = jwtTokenProvider.createToken(member.getEmail());

		return MemberRegisterResponse.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.nickname(member.getNickname())
			.realName(member.getRealName())
			.token(token)
			.build();
	}

	public Member updateMember(Member member, MemberUpdateRequest updateRequest) {
		if (updateRequest.getNickname() != null) {
			if (memberRepository.existsByNickname(updateRequest.getNickname())) {
				throw new CustomException("NICKNAME_DUPLICATE", "이미 사용 중인 닉네임입니다.");
			}
			member.setNickname(updateRequest.getNickname());
		}
		if (updateRequest.getRealName() != null) {
			member.setRealName(updateRequest.getRealName());
		}
		return memberRepository.save(member);
	}
}