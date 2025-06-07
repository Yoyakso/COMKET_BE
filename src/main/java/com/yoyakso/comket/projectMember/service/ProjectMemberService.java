package com.yoyakso.comket.projectMember.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.dto.ProjectMemberInviteRequest;
import com.yoyakso.comket.project.dto.ProjectMemberResponse;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.enums.ProjectMemberState;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
	private final ProjectMemberRepository projectMemberRepository;
	private final MemberService memberService;
	private final ProjectRepository projectRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final WorkspaceMemberService workspaceMemberService;

	public List<ProjectMemberResponse> buildProjectMemberInfoListResponse(Project project, List<Member> assigneeList) {
		return assigneeList.stream()
			.map(assignee -> buildProjectMemberInfoResponse(project, assignee))
			.toList();
	}

	public ProjectMemberResponse buildProjectMemberInfoResponse(Project project, Member assignee) {
		ProjectMember projectMember = getProjectMemberByProjectIdAndMemberId(project.getId(), assignee.getId());
		return ProjectMemberResponse.builder()
			.projectMemberId(projectMember.getId())
			.name(assignee.getFullName())
			.email(assignee.getEmail())
			.positionType(projectMember.getPositionType())
			.state(projectMember.getState())
			.build();
	}

	public ProjectMember addProjectMember(Project project, Member member, String positionType) {
		ProjectMember projectMember = ProjectMember.builder()
			.project(project)
			.member(member)
			.state(ProjectMemberState.ACTIVE)
			.positionType(positionType)
			.build();

		return projectMemberRepository.save(projectMember);
	}

	public ProjectMember getProjectMemberByProjectMemberId(Long projectMemberId) {
		return projectMemberRepository.findById(projectMemberId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_PROJECTMEMBER", "프로젝트 멤버를 찾을 수 없습니다."));
	}

	public ProjectMember getProjectMemberByProjectIdAndMemberId(Long projectId, Long memberId) {
		return projectMemberRepository.findByProjectIdAndMemberIdAndState(projectId, memberId,
				ProjectMemberState.ACTIVE)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_PROJECTMEMBER", "프로젝트 멤버를 찾을 수 없습니다."));
	}

	public List<Project> getProjectListByMemberAndWorkspace(Member member, Workspace workspace) {
		return projectMemberRepository.findAllProjectsByMemberAndWorkspace(member, workspace);
	}

	public Long getMemberIdByProjectMemberId(Long projectMemberId) {
		return projectMemberRepository.findMemberIdById(projectMemberId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_MEMBER_ID", "멤버ID를 찾을 수 없습니다."));
	}

	public List<ProjectMemberResponse> inviteMembersToProject(
		Long projectId,
		ProjectMemberInviteRequest request
	) {
		if (request.getWorkspaceMemberIdList() == null || request.getWorkspaceMemberIdList().isEmpty()) {
			throw new CustomException("INVALID_MEMBER_LIST", "초대할 멤버 ID 리스트가 비어 있습니다.");
		}

		List<Long> newMemberIds = filterNewMemberIds(projectId, request.getWorkspaceMemberIdList());

		return returnInvitedMembersToProject(projectId, request.getPositionType(), newMemberIds);
	}

	public ProjectMember findByMemberEmail(Long projectId, String email) {
		return projectMemberRepository.findByProjectIdAndMemberEmail(projectId, email)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_PROJECTMEMBER", "프로젝트 멤버를 찾을 수 없습니다."));
	}

	public Long findProjectMemberIdByProjectIdAndMemberId(Long projectId, Long memberId) {
		return projectMemberRepository.findIdByProjectIdAndMemberId(projectId, memberId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_PROJECTMEMBER", "프로젝트 멤버를 찾을 수 없습니다."));
	}

	// ---private method---

	private List<Long> filterNewMemberIds(Long projectId, List<Long> WorkspaceMemberIdList) {
		List<Long> existingMemberIds = projectMemberRepository.findAllByProjectId(projectId).stream()
			.filter(projectMember -> projectMember.getState() != ProjectMemberState.DELETED)
			.map(projectMember -> projectMember.getMember().getId())
			.toList();

		List<Long> memberIdList = new ArrayList<>();
		for (Long memberId : WorkspaceMemberIdList) {
			WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberById(memberId);
			memberIdList.add(workspaceMember.getMember().getId());
		}

		List<Long> newMemberIds = memberIdList.stream()
			.filter(memberId -> !existingMemberIds.contains(memberId))
			.toList();

		if (newMemberIds.isEmpty()) {
			throw new CustomException("MEMBER_ALREADY_INVITED", "이미 초대된 멤버가 있습니다.");
		}
		return newMemberIds;
	}

	private List<ProjectMemberResponse> returnInvitedMembersToProject(
		Long projectId,
		String positionType,
		List<Long> newMemberIdList
	) {
		List<ProjectMemberResponse> responseList = new ArrayList<>();

		for (Long newMemberId : newMemberIdList) {
			Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

			Member member = memberService.getMemberById(newMemberId);
			if (member == null) {
				throw new CustomException("CANNOT_FOUND_MEMBER", "멤버를 찾을 수 없습니다.");
			}

			ProjectMember projectMember = ProjectMember.builder()
				.project(project)
				.member(member)
				.state(ProjectMemberState.ACTIVE)
				.positionType(positionType)
				.build();

			ProjectMember newProjectMember = projectMemberRepository.save(projectMember);

			ProjectMemberResponse response = ProjectMemberResponse.builder()
				.projectMemberId(newProjectMember.getId())
				.name(member.getFullName())
				.email(member.getEmail())
				.positionType(newProjectMember.getPositionType())
				.state(newProjectMember.getState())
				.build();

			responseList.add(response);
		}

		return responseList;
	}
}
