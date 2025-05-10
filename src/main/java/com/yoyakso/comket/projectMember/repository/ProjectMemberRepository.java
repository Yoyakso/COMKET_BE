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

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
	Optional<ProjectMember> findByProjectIdAndMemberId(Long workspaceId, Long memberId);

	@Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.member WHERE pm.project.id = :projectId")
	List<ProjectMember> findAllByProjectIdWithMember(@Param("projectId") Long projectId);

	@Query("""
		    SELECT pm.project
		    FROM ProjectMember pm
		    WHERE pm.member = :member
		      AND pm.project.isPublic = true
		""")
	List<Project> findAllPublicProjectsByMember(@Param("member") Member member);

	List<ProjectMember> findAllByProjectId(Long projectId);
}
