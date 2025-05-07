package com.yoyakso.comket.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.enums.FileCategory;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.dto.ProjectMemberResponse;
import com.yoyakso.comket.project.dto.ProjectMemberUpdateRequest;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;

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

	@Override
	public ProjectInfoResponse createProject(String workSpaceName, ProjectCreateRequest request,
		Member member) {
		//TODO: 워크스페이스-멤버에서 권한이 오너, 관리자인 경우에만 프로젝트를 생성할 수 있도록 해야함.

		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		// 생성하는 프로젝트 이름의 중복 검사
		if (projectRepository.existsByName(request.getName())) {
			throw new CustomException("PROJECT_NAME_DUPLICATE", "프로젝트 이름이 중복되었습니다.");
		}
		File profileFile =
			request.getProfileFileId() != null ? fileService.getFileById(request.getProfileFileId()) : null;
		fileService.validateFileCategory(profileFile, FileCategory.PROJECT_PROFILE);
		String profileFileUrl = profileFile != null ? fileService.getFileUrlByPath(profileFile.getFilePath()) : null;

		Project project = Project.builder()
			.workspace(workSpace)
			.name(request.getName())
			.description(request.getDescription())
			.state(ProjectState.ACTIVE) // 초기 상태 예: ACTIVE
			.isPublic(request.getIsPublic())
			.profileFile(profileFile)
			.build();

		Project savedProject = projectRepository.save(project);

		projectMemberService.addProjectMember(project, member, "OWNER");

		return ProjectInfoResponse.builder()
			.projectId(savedProject.getId())
			.projectName(savedProject.getName())
			.profileFileUrl(profileFileUrl)
			.build();
	}

	@Override
	public ProjectInfoResponse updateProject(String workSpaceName, Long projectId, ProjectCreateRequest request,
		Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		File profileFile =
			request.getProfileFileId() != null ? fileService.getFileById(request.getProfileFileId()) : null;
		fileService.validateFileCategory(profileFile, FileCategory.PROJECT_PROFILE);
		String profileFileUrl = profileFile != null ? fileService.getFileUrlByPath(profileFile.getFilePath()) : null;

		ProjectMember projectMember = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		// 프로젝트 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN, OWNER가 아닌 경우
		if (projectMember == null || !projectMember.getIsActive() ||
			(!projectMember.getPositionType().equals("ADMIN") && !projectMember.getPositionType()
				.equals("OWNER"))) {
			throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
		}

		// 수정하는 프로젝트 이름의 중복 검사
		if (projectRepository.existsByName(request.getName())) {
			throw new CustomException("PROJECT_NAME_DUPLICATE", "프로젝트 이름이 중복되었습니다.");
		}

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

		project.updateName(request.getName());
		project.updateDescription(request.getDescription());
		project.updateProjectPublicity(request.getIsPublic());
		project.updateProfileFile(profileFile);
		Project savedProject = projectRepository.save(project);

		return ProjectInfoResponse.builder()
			.projectId(savedProject.getId())
			.projectName(savedProject.getName())
			.profileFileUrl(profileFileUrl)
			.build();
	}

	@Override
	public void patchProjectState(String workSpaceName, Long projectId, Member member,
		ProjectState state) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		ProjectMember projectMember = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		// 프로젝트 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN, OWNER가 아닌 경우
		if (projectMember == null || !projectMember.getIsActive() ||
			(!projectMember.getPositionType().equals("ADMIN") && !projectMember.getPositionType()
				.equals("OWNER"))) {
			throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
		}

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomException("PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."));

		project.updateState(state);
		projectRepository.save(project);
	}

	@Override
	public void exitProject(String workSpaceName, Long projectId, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		ProjectMember projectMember = projectMemberService.getProjectMemberByProjectIdAndMemberId(projectId,
			member.getId());

		// 프로젝트 멤버가 아닐 경우
		if (projectMember == null || !projectMember.getIsActive()) {
			throw new CustomException("NOT_PROJECT_MEMBER", "이미 프로젝트 멤버가 아닙니다.");
		}

		projectMember.updateIsActive(false);
		projectMemberRepository.save(projectMember);
	}

	@Override
	public List<ProjectInfoResponse> getAllProjects(String workSpaceName, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		List<Project> projects = projectRepository.findAllByWorkspaceAndIsPublicTrue(workSpace);

		return projects.stream()
			.map(project -> {
				String profileFileUrl = project.getProfileFile() != null
					? fileService.getFileUrlByPath(project.getProfileFile().getFilePath())
					: null;
				return new ProjectInfoResponse(project.getId(), project.getName(), profileFileUrl);
			})
			.toList();
	}

	@Override
	public List<ProjectInfoResponse> getAllProjectsByMember(String workSpaceName, Member member) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		List<Project> projects = projectMemberService.getProjectListByMemberId(member);

		return projects.stream()
			.map(project -> {
				String profileFileUrl = project.getProfileFile() != null
					? fileService.getFileUrlByPath(project.getProfileFile().getFilePath())
					: null;
				return new ProjectInfoResponse(project.getId(), project.getName(), profileFileUrl);
			})
			.toList();
	}

	@Override
	public List<ProjectMemberResponse> getProjectMembers(String workSpaceName, Long projectId) {
		// 워크스페이스 조회
		Workspace workSpace = workspaceRepository.findByName(workSpaceName)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		List<ProjectMember> projectMembers = projectMemberRepository.findAllByProjectIdWithMember(projectId);

		return projectMembers.stream()
			.map(pm -> {
				Member member = pm.getMember();
				return ProjectMemberResponse.builder()
					.name(member.getRealName())
					.email(member.getEmail())
					.positionType(pm.getPositionType())
					.isActive(pm.getIsActive())
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

		// 프로젝트 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN, OWNER가 아닌 경우
		if (updateRequester == null || !updateRequester.getIsActive() ||
			(!updateRequester.getPositionType().equals("ADMIN") && !updateRequester.getPositionType()
				.equals("OWNER"))) {
			throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
		}

		// 변경 대상 검증
		ProjectMember projectMember = projectMemberRepository.findById(request.getProjectMemberId())
			.orElseThrow(() -> new CustomException("PROJECTMEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다."));

		projectMember.updatePositionType(request.getPositionType());
		ProjectMember updatedProjectMember = projectMemberRepository.save(projectMember);
		Member updatedMember = updatedProjectMember.getMember();

		return ProjectMemberResponse.builder()
			.name(updatedMember.getRealName())
			.email(updatedMember.getEmail())
			.isActive(updatedProjectMember.getIsActive())
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

		// 프로젝트 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN, OWNER가 아닌 경우
		if (updateRequester == null || !updateRequester.getIsActive() ||
			(!updateRequester.getPositionType().equals("ADMIN") && !updateRequester.getPositionType()
				.equals("OWNER"))) {
			throw new CustomException("PROJECT_AUTHORIZATION_FAILED", "프로젝트에 대한 권한이 없습니다.");
		}

		// 변경 대상 검증
		ProjectMember projectMember = projectMemberRepository.findById(projectMemberId)
			.orElseThrow(() -> new CustomException("PROJECTMEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다."));

		projectMember.updateIsActive(false);
		projectMemberRepository.save(projectMember);
	}
}
