package com.yoyakso.comket.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Member findByEmail(String email);

	boolean existsByEmail(String email);
}