package com.yoyakso.comket.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.dto.WorkspaceInfoResponse;
import com.yoyakso.comket.workspace.dto.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.Visibility;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceMemberService workspaceMemberService;
	private final FileService fileService;

	public Workspace registerWorkspace(WorkspaceRegisterRequest workspaceRegisterRequest, Member member) {
		Workspace workspace = Workspace.fromRequest(workspaceRegisterRequest);
		if (workspaceRegisterRequest.getProfileFileId() != null) {
			File profileFile = fileService.getFileById(workspaceRegisterRequest.getProfileFileId());
			workspace.setProfileFile(profileFile);
		}
		validateWorkspaceNameUniqueness(workspace.getName());
		Workspace savedWorkspace = workspaceRepository.save(workspace);
		workspaceMemberService.createWorkspaceMember(savedWorkspace, member, true, "ADMIN");
		return savedWorkspace;
	}

	public Workspace getWorkspaceById(Long id, Member member) {
		Workspace workspace = findWorkspaceById(id);
		validateWorkspaceAccess(workspace, member);
		return workspace;
	}

	public List<Workspace> getAllWorkspaces() {
		return workspaceRepository.findAll();
	}

	public Workspace updateWorkspace(Member member, Long id, Workspace workspace) {
		Workspace originalWorkspace = findWorkspaceById(id);
		validateUpdatePermission(member, id);
		validateWorkspaceNameUniquenessForUpdate(workspace.getName(), originalWorkspace.getName());
		updateWorkspaceDetails(originalWorkspace, workspace);
		return workspaceRepository.save(originalWorkspace);
	}

	public void deleteWorkspace(Long id, Member member) {
		Workspace workspace = findWorkspaceById(id);
		validateDeletePermission(member, id);
		if (workspace.isDeleted()) {
			throw new CustomException("WORKSPACE_ALREADY_DELETED", "이미 삭제된 워크스페이스입니다.");
		}
		workspace.setDeleted(true);
		workspaceRepository.save(workspace);
	}

	public List<Workspace> getWorkspacesByMember(Member member) {
		return workspaceMemberService.getWorkspacesByMember(member);
	}

	public List<Workspace> getPublicWorkspaces() {
		return workspaceRepository.findAll().stream()
			.filter(workspace -> Visibility.PUBLIC.equals(workspace.getVisibility()))
			.filter(workspace -> !workspace.isDeleted())
			.collect(Collectors.toList());
	}

	// --- Private Helper Methods ---

	private Workspace findWorkspaceById(Long id) {
		return workspaceRepository.findById(id)
			.filter(ws -> !ws.isDeleted())
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
	}

	private void validateWorkspaceNameUniqueness(String name) {
		if (workspaceRepository.existsByName(name)) {
			throw new CustomException("WORKSPACE_NAME_DUPLICATE", "워크스페이스 이름이 중복되었습니다.");
		}
	}

	private void validateWorkspaceNameUniquenessForUpdate(String newName, String currentName) {
		if (workspaceRepository.existsByName(newName) && !currentName.equals(newName)) {
			throw new CustomException("WORKSPACE_NAME_DUPLICATE", "워크스페이스 이름이 중복되었습니다.");
		}
	}

	private void validateWorkspaceAccess(Workspace workspace, Member member) {
		WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(
			workspace.getId(), member.getId());
		if (workspace.getVisibility().equals(Visibility.PRIVATE) && workspaceMember == null) {
			throw new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다.");
		}
	}

	private void validateUpdatePermission(Member member, Long workspaceId) {
		WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(
			workspaceId, member.getId());
		if (workspaceMember == null || !workspaceMember.isActive() ||
			(!"ADMIN".equals(workspaceMember.getPositionType()) && !"OWNER".equals(
				workspaceMember.getPositionType()))) {
			throw new CustomException("WORKSPACE_AUTHORIZATION_FAILED", "워크스페이스에 대한 권한이 없습니다.");
		}
	}

	private void validateDeletePermission(Member member, Long workspaceId) {
		validateUpdatePermission(member, workspaceId); // 삭제 권한은 업데이트 권한과 동일
	}

	private void updateWorkspaceDetails(Workspace originalWorkspace, Workspace updatedWorkspace) {
		originalWorkspace.setName(updatedWorkspace.getName());
		originalWorkspace.setDescription(updatedWorkspace.getDescription());
		originalWorkspace.setVisibility(updatedWorkspace.getVisibility());
		originalWorkspace.setUpdatedAt(updatedWorkspace.getUpdatedAt());
	}

	public WorkspaceInfoResponse toResponse(Workspace workspace) {
		String profileFileUrl = null;
		if (workspace.getProfileFile() != null) {
			File profileFile = fileService.getFileById(workspace.getProfileFile().getId());
			profileFileUrl = fileService.getFileUrlByPath(profileFile.getFilePath());
		}

		return WorkspaceInfoResponse.builder()
			.id(workspace.getId())
			.name(workspace.getName())
			.description(workspace.getDescription())
			.visibility(workspace.getVisibility())
			.profileFileUrl(profileFileUrl)
			.createdAt(workspace.getCreatedAt().toString())
			.updatedAt(workspace.getUpdatedAt().toString())
			.build();
	}
}