package com.yoyakso.comket.workspaceMember.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
	Optional<WorkspaceMember> findByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

	List<WorkspaceMember> findByMember(Member member);

	List<WorkspaceMember> findByWorkspaceId(Long id);
}
