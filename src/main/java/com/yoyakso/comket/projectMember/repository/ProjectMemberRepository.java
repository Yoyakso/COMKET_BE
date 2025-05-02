package com.yoyakso.comket.projectMember.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.projectMember.entity.ProjectMember;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
	Optional<ProjectMember> findByProjectIdAndMemberId(Long workspaceId, Long memberId);

	List<Project> findAllProjectByMemberAndIsActiveTrue(Member member);
}
