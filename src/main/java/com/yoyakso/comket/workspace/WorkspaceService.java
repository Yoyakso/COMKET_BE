package com.yoyakso.comket.workspace;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.enums.FileCategory;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.dto.WorkspaceInfoResponse;
import com.yoyakso.comket.workspace.dto.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.dto.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.WorkspaceState;
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
			fileService.validateFileCategory(profileFile, FileCategory.WORKSPACE_PROFILE);
			workspace.setProfileFile(profileFile);
		}
		validateWorkspaceNameUniqueness(workspace.getName());
		validateWorkspaceSlugUniqueness(workspace.getSlug());
		workspace.setInviteCode(generateUniqueInviteCode()); // 중복 검사 후 초대 코드 설정
		Workspace savedWorkspace = workspaceRepository.save(workspace);
		workspaceMemberService.createWorkspaceMember(savedWorkspace, member, true, "OWNER");
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

	public Workspace updateWorkspace(Member member, Long id, WorkspaceUpdateRequest workspaceUpdateRequest) {
		Workspace workspace = Workspace.fromRequest(workspaceUpdateRequest);
		Workspace originalWorkspace = findWorkspaceById(id);
		if (workspaceUpdateRequest.getProfileFileId() != null) {
			File profileFile = fileService.getFileById(workspaceUpdateRequest.getProfileFileId());
			fileService.validateFileCategory(profileFile, FileCategory.WORKSPACE_PROFILE);
			workspace.setProfileFile(profileFile);
		}
		validateUpdatePermission(member, id);
		validateWorkspaceNameUniquenessForUpdate(workspace.getName(), originalWorkspace.getName());
		validateWorkspaceSlugUniquenessForUpdate(workspace.getSlug(), originalWorkspace.getSlug());
		updateWorkspaceDetails(originalWorkspace, workspace);
		return workspaceRepository.save(originalWorkspace);
	}

	public void deleteWorkspace(Long id, Member member) {
		Workspace workspace = findWorkspaceById(id);
		validateDeletePermission(member, id);
		if (workspace.getState() == WorkspaceState.DELETED) {
			throw new CustomException("WORKSPACE_ALREADY_DELETED", "이미 삭제된 워크스페이스입니다.");
		}
		workspace.setState(WorkspaceState.DELETED);
		workspaceRepository.save(workspace);
	}

	public List<Workspace> getWorkspacesByMember(Member member) {
		return workspaceMemberService.getWorkspacesByMember(member);
	}

	public List<Workspace> getPublicWorkspaces() {
		return workspaceRepository.findAll().stream()
			.filter(Workspace::getIsPublic)
			.filter(workspace -> workspace.getState() == WorkspaceState.ACTIVE)
			.collect(Collectors.toList());
	}

	// --- Private Helper Methods ---

	private Workspace findWorkspaceById(Long id) {
		return workspaceRepository.findById(id)
			.filter(ws -> !ws.getState().equals(WorkspaceState.DELETED))
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
		if (!workspace.getIsPublic() && workspaceMember == null) {
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

	private void validateWorkspaceSlugUniqueness(String slug) {
		if (workspaceRepository.existsBySlug(slug)) {
			throw new CustomException("WORKSPACE_SLUG_DUPLICATE", "워크스페이스 슬러그가 중복되었습니다.");
		}
	}

	private void validateWorkspaceSlugUniquenessForUpdate(String newSlug, String currentSlug) {
		if (workspaceRepository.existsBySlug(newSlug) && !currentSlug.equals(newSlug)) {
			throw new CustomException("WORKSPACE_SLUG_DUPLICATE", "워크스페이스 슬러그가 중복되었습니다.");
		}
	}

	private void updateWorkspaceDetails(Workspace originalWorkspace, Workspace updatedWorkspace) {
		originalWorkspace.setName(updatedWorkspace.getName());
		originalWorkspace.setDescription(updatedWorkspace.getDescription());
		originalWorkspace.setIsPublic(updatedWorkspace.getIsPublic());
		originalWorkspace.setState(updatedWorkspace.getState());
		originalWorkspace.setUpdatedAt(updatedWorkspace.getUpdatedAt());
	}

	private String generateUniqueInviteCode() {
		String inviteCode;
		Random random = new Random();
		do {
			inviteCode = String.format("%06d", random.nextInt(1000000)); // 6자리 숫자 생성
		} while (workspaceRepository.existsByInviteCode(inviteCode)); // 중복 검사
		return inviteCode;
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
			.profileFileUrl(profileFileUrl)
			.isPublic(workspace.getIsPublic())
			.state(workspace.getState())
			.inviteCode(workspace.getInviteCode())
			.slug(workspace.getSlug())
			.createdAt(workspace.getCreatedAt().toString())
			.updatedAt(workspace.getUpdatedAt().toString())
			.build();
	}
}