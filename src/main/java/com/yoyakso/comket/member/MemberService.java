package com.yoyakso.comket.member;

import org.springframework.stereotype.Service;

@Service
public class MemberService {
	private final MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmail(email);
	}

	public Member findByPhoneNumber(String phoneNumber) {
		return memberRepository.findByPhoneNumber(phoneNumber);
	}

	public Member findByName(String name) {
		return memberRepository.findByName(name);
	}

	public Member findByIdAndName(Long id, String name) {
		return memberRepository.findByIdAndName(id, name);
	}
}
