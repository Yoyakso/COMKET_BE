package com.yoyakso.comket.projectMember.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.enums.ProjectMemberState;
import com.yoyakso.comket.workspace.entity.Workspace;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
	Optional<ProjectMember> findByProjectIdAndMemberIdAndState(Long projectId, Long memberId, ProjectMemberState state);

	Optional<ProjectMember> findByProjectIdAndMemberEmail(Long projectId, String email);

	@Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.member WHERE pm.project.id = :projectId")
	List<ProjectMember> findAllByProjectIdWithMember(@Param("projectId") Long projectId);

	@Query("""
		    SELECT pm.project
		    FROM ProjectMember pm
		    WHERE pm.member = :member
		    And pm.project.workspace = :workspace
		""")
	List<Project> findAllProjectsByMemberAndWorkspace(
		@Param("member") Member member,
		@Param("workspace") Workspace workspace
	);

	@Query("SELECT pm.id FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.member.id = :memberId")
	Optional<Long> findIdByProjectIdAndMemberId(@Param("projectId") Long projectId, @Param("memberId") Long memberId);

	@Query("SELECT pm.member.id FROM ProjectMember pm WHERE pm.id = :id")
	Optional<Long> findMemberIdById(@Param("id") Long id);

	List<ProjectMember> findAllByProjectId(Long projectId);

	// 기존 OWNER는 Unique 값이라 상관 없었지만, ADMIN은 다수일 수 있기 때문에 가장 초기에 ADMIN인 유저 추출
	ProjectMember findFirstByProjectIdAndPositionTypeOrderByUpdatedAtAsc(Long projectId, String positionType);
}
