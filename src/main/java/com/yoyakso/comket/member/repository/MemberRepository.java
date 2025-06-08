package com.yoyakso.comket.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByEmail(String email);

	Optional<Member> findByEmail(String email);

	// 삭제되지 않은 회원만 조회하는 메서드 추가
	Optional<Member> findByEmailAndIsDeletedFalse(String email);

	// 삭제된 회원 조회 메서드 추가
	Optional<Member> findByEmailAndIsDeletedTrue(String email);
}
