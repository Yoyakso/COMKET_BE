package com.yoyakso.comket.projectMember.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
	private final ProjectMemberRepository memberRepository;

	public void addProjectMember(Project project, Member member, String positionType) {
		ProjectMember projectMember = ProjectMember.builder()
			.project(project)
			.member(member)
			.isActive(true)
			.positionType(positionType)
			.build();

		memberRepository.save(projectMember);
	}

	public ProjectMember getProjectMemberByProjectIdAndMemberId(Long projectId, Long memberId) {
		return memberRepository.findByProjectIdAndMemberId(projectId, memberId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_PROJECTMEMBER", "프로젝트 멤버를 찾을 수 없습니다."));
	}

	public List<Project> getProjectListByMemberId(Member member) {
		return memberRepository.findAllProjectByMemberAndIsActiveTrue(member);
	}
}
