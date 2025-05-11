package com.yoyakso.comket.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.dto.ProjectMemberInviteRequest;
import com.yoyakso.comket.project.dto.ProjectMemberResponse;
import com.yoyakso.comket.project.dto.ProjectMemberUpdateRequest;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;

@Service
public interface ProjectService {
	ProjectInfoResponse createProject(String workSpaceName, ProjectCreateRequest request,
		Member member);

	ProjectInfoResponse updateProject(String workSpaceName, Long projectId, ProjectCreateRequest request,
		Member member);

	ProjectInfoResponse getProject(String workSpaceName, Long projectId, Member member);

	void patchProjectState(String workSpaceName, Long projectId, Member member, ProjectState state);

	void exitProject(String workSpaceName, Long projectId, Member member);

	void deleteProjectMember(String workSpaceName, Long projectId, Member member, Long projectMemberId);

	List<ProjectInfoResponse> getAllProjects(String workSpaceName, Member member);

	List<ProjectInfoResponse> getAllProjectsByMember(String workSpaceName, Member member);

	List<ProjectMemberResponse> getProjectMembers(String workSpaceName, Long projectId);

	ProjectMemberResponse patchProjectMembersPosition(String workSpaceName, Long projectId, Member member,
		ProjectMemberUpdateRequest request);

	ProjectMemberResponse assignNewOwner(String workSpaceName, Long projectId, Member member,
		Long targetMemberId);

	List<ProjectMemberResponse> inviteProjectMembers(String workSpaceName, Long projectId, Member member,
		ProjectMemberInviteRequest request);

	Project getProjectByProjectName(String projectName);

	void validateProjectAccess(Project project, Member member, String target);
}
