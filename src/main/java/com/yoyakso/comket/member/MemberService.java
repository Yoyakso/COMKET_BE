package com.yoyakso.comket.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.dto.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.MemberUpdateRequest;

@Service
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public Member saveMember(MemberRegisterRequest memberRegisterRequest) {
		if (memberRepository.existsByEmail(memberRegisterRequest.getEmail())) {
			throw new CustomException("EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다.");
		}

		if (memberRepository.existsByNickname(memberRegisterRequest.getNickname())) {
			throw new CustomException("NICKNAME_DUPLICATE", "이미 사용 중인 닉네임입니다.");
		}

		Member member = new Member();
		member.setEmail(memberRegisterRequest.getEmail());
		member.setPassword(passwordEncoder.encode(memberRegisterRequest.getPassword()));
		member.setNickname(memberRegisterRequest.getNickname());
		member.setRealName(memberRegisterRequest.getRealName());

		memberRepository.save(member);
		return member;
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
}