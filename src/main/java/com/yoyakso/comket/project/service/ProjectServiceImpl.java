package com.yoyakso.comket.project.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.enums.FileCategory;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.dto.ProjectMemberInviteRequest;
import com.yoyakso.comket.project.dto.ProjectMemberResponse;
import com.yoyakso.comket.project.dto.ProjectMemberUpdateRequest;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.enums.ProjectMemberState;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {
	private final ProjectRepository projectRepository;
	private final WorkspaceRepository workspaceRepository;
	private final ProjectMemberService projectMemberService;
	private final ProjectMemberRepository projectMemberRepository;
	private final FileService fileService;
	private final WorkspaceMemberService workspaceMemberService;

	@Override
	public ProjectInfoResponse createProject(String workSpaceName, ProjectCreateRequest request,
		Member member) {

		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		// 생성하는 프로젝트 이름의 중복 검사
		if (projectRepository.existsByNameAndStateAndWorkspace(request.getName(), ProjectState.ACTIVE, workSpace)) {
			throw new CustomException("PROJECT_NAME_DUPLICATE", "프로젝트 이름이 중복되었습니다.");
		}

		List<String> tags = deduplicateTags(request.getTags());

		File profileFile =
			request.getProfileFileId() != null ? fileService.getFileById(request.getProfileFileId()) : null;
		fileService.validateFileCategory(profileFile, FileCategory.MEMBER_PROFILE);
		String profileFileUrl = profileFile != null ? fileService.getFileUrlByPath(profileFile.getFilePath()) : null;

		Project project = Project.builder()
			.workspace(workSpace)
			.name(request.getName())
			.description(request.getDescription())
			.tags(tags)
			.state(ProjectState.ACTIVE) // 초기 상태 예: ACTIVE
			.isPublic(request.getIsPublic())
			.profileFile(profileFile)
			.build();

		Project savedProject = projectRepository.save(project);

		ProjectMember pm = projectMemberService.addProjectMember(savedProject, member, "ADMIN");

		ProjectMemberResponse adminInfo = returnAdminInfo(pm);

		return ProjectInfoResponse.builder()
			.projectId(savedProject.getId())
			.projectName(savedProject.getName())
			.projectDescription(savedProject.getDescription())
			.projectTag(savedProject.getTags())
			.isPublic(savedProject.getIsPublic())
			.createTime(savedProject.getCreateTime())
			.adminInfo(adminInfo)
			.profileFileUrl(profileFileUrl)
			.build();
	}

	@Override
	public ProjectInfoResponse updateProject(String workSpaceName, Long projectId, ProjectCreateRequest request,
		Member member) {
		// 워크스페이스 조회
		Workspace workspace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		File profileFile =
			request.getProfileFileId() != null ? fileService.getFileById(request.getProfileFileId()) : null;
		fileService.validateFileCategory(profileFile, FileCategory.MEMBER_PROFILE);
		String profileFileUrl = profileFile != null ? fileService.getFileUrlByPath(profileFile.getFilePath()) : null;

		ProjectMember updateRequester = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		// 프로젝트 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN이 아닌 경우
		if (updateRequester == null || updateRequester.getState() != ProjectMemberState.ACTIVE ||
			(!updateRequester.getPositionType().equals("ADMIN"))) {
			throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
		}

		Project originProject = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

		// 수정하는 프로젝트 이름의 중복 검사, 프로젝트 수정의 경우 기존 프로젝트 이름도 중복 처리되어 비교 추가
		if (projectRepository.existsByNameAndStateAndWorkspace(request.getName(), ProjectState.ACTIVE, workspace)
			&& (!Objects.equals(
			originProject.getName(),
			request.getName()))) {
			throw new CustomException("PROJECT_NAME_DUPLICATE", "프로젝트 이름이 중복되었습니다.");
		}

		List<String> tags = deduplicateTags(request.getTags());

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

		project.updateName(request.getName());
		project.updateDescription(request.getDescription());
		project.updateProjectPublicity(request.getIsPublic());
		project.updateProfileFile(profileFile);
		project.updateTags(tags);
		Project savedProject = projectRepository.save(project);

		ProjectMember pm = projectMemberRepository.findFirstByProjectIdAndPositionTypeOrderByUpdatedAtAsc(
			project.getId(), "ADMIN");
		ProjectMemberResponse adminInfo = returnAdminInfo(pm);

		return ProjectInfoResponse.builder()
			.projectId(savedProject.getId())
			.projectName(savedProject.getName())
			.projectDescription(savedProject.getDescription())
			.isPublic(savedProject.getIsPublic())
			.projectTag(savedProject.getTags())
			.createTime(savedProject.getCreateTime())
			.adminInfo(adminInfo)
			.profileFileUrl(profileFileUrl)
			.build();
	}

	@Override
	public void patchProjectState(String workSpaceName, Long projectId, Member member,
		ProjectState state) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

		ProjectMember updateRequester = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		validateAdminPermission(updateRequester.getMember(), projectId);

		project.updateState(state);
		projectRepository.save(project);
	}

	@Override
	public void exitProject(String workSpaceName, Long projectId, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		ProjectMember updateRequester = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		// 프로젝트 멤버가 아닐 경우
		if (updateRequester == null || updateRequester.getState() == ProjectMemberState.DELETED) {
			throw new CustomException("NOT_PROJECT_MEMBER", "이미 프로젝트 멤버가 아닙니다.");
		}

		validateAdminPermission(updateRequester, updateRequester);

		updateRequester.updateMemberState(ProjectMemberState.DELETED);
		projectMemberRepository.save(updateRequester);
	}

	@Override
	public ProjectInfoResponse getProject(String workSpaceName, Long projectId, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

		String profileFileUrl = project.getProfileFile() != null
			? fileService.getFileUrlByPath(project.getProfileFile().getFilePath())
			: null;

		ProjectMember pm = projectMemberRepository.findFirstByProjectIdAndPositionTypeOrderByUpdatedAtAsc(
			project.getId(), "ADMIN");
		ProjectMemberResponse adminInfo = returnAdminInfo(pm);

		return ProjectInfoResponse.builder()
			.projectId(project.getId())
			.projectName(project.getName())
			.projectDescription(project.getDescription())
			.projectTag(project.getTags())
			.adminInfo(adminInfo)
			.isPublic(project.getIsPublic())
			.createTime(project.getCreateTime())
			.profileFileUrl(profileFileUrl)
			.build();
	}

	@Override
	public List<ProjectInfoResponse> getAllProjects(String workSpaceName, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		// isPublic = true인 경우 모두 리턴 + isPublic = false인 경우 내가 속한 경우에만 리턴
		List<Project> projects = projectRepository.findProjectsByWorkspaceAndMemberAndState(workSpace.getId(),
			member.getId(), ProjectState.ACTIVE);

		return projects.stream()
			.map(project -> {
				String profileFileUrl = project.getProfileFile() != null
					? fileService.getFileUrlByPath(project.getProfileFile().getFilePath())
					: null;
				ProjectMember pm = projectMemberRepository.findFirstByProjectIdAndPositionTypeOrderByUpdatedAtAsc(
					project.getId(), "ADMIN");
				ProjectMemberResponse adminInfo = returnAdminInfo(pm);
				return ProjectInfoResponse.builder()
					.projectId(project.getId())
					.projectName(project.getName())
					.projectDescription(project.getDescription())
					.projectTag(project.getTags())
					.adminInfo(adminInfo)
					.isPublic(project.getIsPublic())
					.createTime(project.getCreateTime())
					.profileFileUrl(profileFileUrl)
					.build();
			})
			.toList();
	}

	@Override
	public List<ProjectInfoResponse> getAllProjectsByMember(String workSpaceName, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		List<Project> projects = projectMemberService.getProjectListByMemberAndWorkspace(member, workSpace);

		return projects.stream()
			.map(project -> {
				String profileFileUrl = project.getProfileFile() != null
					? fileService.getFileUrlByPath(project.getProfileFile().getFilePath())
					: null;

				ProjectMember pm = projectMemberRepository.findFirstByProjectIdAndPositionTypeOrderByUpdatedAtAsc(
					project.getId(), "ADMIN");
				ProjectMemberResponse adminInfo = returnAdminInfo(pm);
				return ProjectInfoResponse.builder()
					.projectId(project.getId())
					.projectName(project.getName())
					.projectDescription(project.getDescription())
					.projectTag(project.getTags())
					.adminInfo(adminInfo)
					.isPublic(project.getIsPublic())
					.createTime(project.getCreateTime())
					.profileFileUrl(profileFileUrl)
					.build();
			})
			.toList();
	}

	@Override
	public List<ProjectMemberResponse> inviteProjectMembers(
		String workSpaceName,
		Long projectId,
		Member member,
		ProjectMemberInviteRequest request
	) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		validateAdminPermission(member, projectId);

		return projectMemberService.inviteMembersToProject(projectId, request);
	}

	@Override
	public List<ProjectMemberResponse> getProjectMembers(String workSpaceName, Long projectId) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		List<ProjectMember> projectMembers = projectMemberRepository.findAllByProjectIdWithMember(projectId);

		return projectMembers.stream()
			.map(pm -> {
				Long workspaceMemberId = projectMemberRepository.findWorkspaceMemberIdByProjectMemberId(
					pm.getId());
				WorkspaceMember wm = workspaceMemberService.getWorkspaceMemberById(workspaceMemberId);

				File profileFile =
					wm.getProfileFile() != null ? fileService.getFileById(wm.getProfileFile().getId()) : null;
				fileService.validateFileCategory(profileFile, FileCategory.MEMBER_PROFILE);
				String profileFileUrl =
					profileFile != null ? fileService.getFileUrlByPath(profileFile.getFilePath()) : null;

				Member member = pm.getMember();
				return ProjectMemberResponse.builder()
					.projectMemberId(pm.getId())
					.name(member.getFullName())
					.email(member.getEmail())
					.positionType(pm.getPositionType())
					.state(pm.getState())
					.workspaceMemberId(wm.getId())
					.profileUri(profileFileUrl)
					.build();
			})
			.collect(Collectors.toList());
	}

	@Override
	public ProjectMemberResponse patchProjectMembersPosition(
		String workSpaceName,
		Long projectId,
		Member member,
		ProjectMemberUpdateRequest request
	) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		// API 사용 유저 권한 검증
		ProjectMember updateRequester = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());
		validateAdminPermission(updateRequester.getMember(), projectId);

		// 변경 대상 검증
		ProjectMember projectMember = projectMemberRepository.findById(request.getProjectMemberId())
			.orElseThrow(() -> new CustomException("PROJECTMEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다."));

		// 상위 권한 검증
		validateUpperCasePermission(updateRequester, projectMember);

		validateAdminPermission(updateRequester, projectMember);

		projectMember.updatePositionType(request.getPositionType());
		ProjectMember updatedProjectMember = projectMemberRepository.save(projectMember);
		Member updatedMember = updatedProjectMember.getMember();

		return ProjectMemberResponse.builder()
			.projectMemberId(updatedMember.getId())
			.name(updatedMember.getFullName())
			.email(updatedMember.getEmail())
			.state(updatedProjectMember.getState())
			.positionType(updatedProjectMember.getPositionType())
			.build();
	}

	@Override
	public void deleteProjectMember(
		String workSpaceName,
		Long projectId,
		Member member,
		Long projectMemberId
	) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		// API 사용 유저 권한 검증
		ProjectMember updateRequester = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		validateAdminPermission(updateRequester.getMember(), projectId);

		// 변경 대상 검증
		ProjectMember projectMember = projectMemberRepository.findById(projectMemberId)
			.orElseThrow(() -> new CustomException("PROJECTMEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다."));

		validateAdminPermission(updateRequester, projectMember);

		projectMember.updateMemberState(ProjectMemberState.DELETED);
		projectMemberRepository.save(projectMember);
	}

	@Override
	public Project getProjectByProjectName(String projectName) {
		return projectRepository.findByName(projectName)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));
	}

	@Override
	public void validateProjectAccess(Project project, Member member, String target) {
		// 프로젝트 멤버가 존재하지 않거나 비활성화된 경우
		ProjectMember projectMember = projectMemberService.getProjectMemberByProjectIdAndMemberId(project.getId(),
			member.getId());
		if (projectMember == null || projectMember.getState() != ProjectMemberState.ACTIVE) {
			throw new CustomException("PROJECT_ACCESS_FAILED", target + "에 대한 프로젝트에 대한 권한이 없습니다.");
		}
	}

	@Override
	public List<Project> getProjectsByWorkspaceAndMember(Workspace workspace, Member member) {
		return projectMemberService.getProjectListByMemberAndWorkspace(member, workspace);
	}

	public ProjectMember validateAdminPermission(Member member, Long projectId) {
		ProjectMember updateRequester = projectMemberService.getProjectMemberByProjectIdAndMemberId(
			projectId,
			member.getId()
		);

		String positionType = updateRequester.getPositionType();
		if (updateRequester == null || updateRequester.getState() != ProjectMemberState.ACTIVE || (positionType.equals(
			"MEMBER"))) {
			throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
		}

		return updateRequester;
	}

	@Override
	public Project getProjectNameById(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));
	}

	@Override
	public List<Project> getProjectsByWorkspaceId(Long workspaceId, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findById(workspaceId)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		// 워크스페이스 멤버 검증
		workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(workSpace.getId(), member.getId());

		return projectRepository.findAllByWorkspaceAndState(workSpace, ProjectState.ACTIVE);
	}

	@Override
	public Project getProjectByProjectId(Long projectId, Member member) {
		// 프로젝트 조회
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));
		// 프로젝트 멤버 검증
		ProjectMember projectMember = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());
		return project;
	}

	// ---private methods---
	private void validateUpperCasePermission(ProjectMember controllerMember, ProjectMember targetMember) {
		// ADMIN은 ADMIN과 MEMBER의 포지션과 상태를 변경할 수 있다.
		if ("ADMIN".equals(controllerMember.getPositionType()) &&
			("ADMIN".equals(targetMember.getPositionType()) || "MEMBER".equals(targetMember.getPositionType()))) {
			return;
		}
		// MEMBER는 자신의 포지션과 상태만 변경할 수 있다.
		if ("MEMBER".equals(controllerMember.getPositionType()) &&
			controllerMember.getId().equals(targetMember.getId())) {
			return;
		}
		throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
	}

	// Set으로 중복된 태그를 제거
	private List<String> deduplicateTags(List<String> originTags) {
		if (originTags == null) {
			return Collections.emptyList();
		}

		Set<String> uniqueTags = new HashSet<>(originTags);
		return new ArrayList<>(uniqueTags);
	}

	private void validateAdminPermission(ProjectMember member, ProjectMember targetMember) {
		if (member.getPositionType().equals("ADMIN") && (member.equals(targetMember))) {
			throw new CustomException("ADMIN_EXCEPTION", "관리자 권한 이전이 필요합니다.");
		}
	}

	private ProjectMemberResponse returnAdminInfo(ProjectMember pm) {
		return ProjectMemberResponse.builder()
			.projectMemberId(pm.getId())
			.name(pm.getMember().getFullName())
			.email(pm.getMember().getEmail())
			.positionType(pm.getPositionType())
			.state(pm.getState())
			.build();
	}
}
