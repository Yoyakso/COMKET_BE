package com.yoyakso.comket.workspaceMember.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
	Optional<WorkspaceMember> findByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

	List<WorkspaceMember> findByMember(Member member);

	List<WorkspaceMember> findByWorkspaceId(Long id);

	@Query("SELECT wm FROM WorkspaceMember wm " +
		"WHERE wm.workspace.id = :workspaceId " +
		"AND (:keyword IS NULL OR wm.member.fullName LIKE %:keyword% OR wm.member.email LIKE %:keyword%) " +
		"AND (:positionTypes IS NULL OR wm.positionType IN :positionTypes) " +
		"AND (:memberStates IS NULL OR wm.state IN :memberStates)")
	List<WorkspaceMember> searchWorkspaceMembers(
		@Param("workspaceId") Long workspaceId,
		@Param("keyword") String keyword,
		@Param("positionTypes") List<String> positionTypes,
		@Param("memberStates") List<String> memberStates
	);

	List<WorkspaceMember> findAllByWorkspaceIdAndMemberIdInAndState(Long workspaceId, List<Long> memberIds,
		WorkspaceMemberState state);

	WorkspaceMember findByMemberIdAndWorkspaceId(Long memberId, Long workspaceId);
}
