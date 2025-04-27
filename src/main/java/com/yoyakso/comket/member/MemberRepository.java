package com.yoyakso.comket.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Member findByEmail(String email);

	Member findByPhoneNumber(String phoneNumber);

	Member findByName(String name);

	Member findByIdAndName(Long id, String name);
}