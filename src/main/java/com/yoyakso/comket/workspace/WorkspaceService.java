package com.yoyakso.comket.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
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

	public Workspace registerWorkspace(Workspace workspace, Member member) {
		if (workspaceRepository.existsByName(workspace.getName())) {
			throw new CustomException("WORKSPACE_NAME_DUPLICATE", "워크스페이스 이름이 중복되었습니다.");
		}
		// 워크스페이스 생성
		Workspace savedWorkspace = workspaceRepository.save(workspace);
		// 워크스페이스 멤버 추가
		workspaceMemberService.createWorkspaceMember(savedWorkspace, member, true, "ADMIN");
		return savedWorkspace;
	}

	public Workspace getWorkspaceById(Long id, Member member) {
		Workspace workspace = workspaceRepository.findById(id)
			.filter(ws -> !ws.isDeleted())
			.orElse(null);

		WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(id,
			member.getId());
		// 1. 워크스페이스가 존재하지 않거나
		// 2. 워크스페이스가 비공개이고, 해당 멤버가 워크스페이스에 속하지 않는 경우
		if (workspace == null || (workspace.getVisibility().equals(Visibility.PRIVATE) && workspaceMember == null)) {
			throw new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다.");
		}
		return workspace;
	}

	public List<Workspace> getAllWorkspaces() {
		return workspaceRepository.findAll();
	}

	public Workspace updateWorkspace(Member member, Long id, Workspace workspace) {
		// 워크스페이스 멤버 확인
		WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(id,
			member.getId());
		// 워크스페이스 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN, OWNER가 아닌 경우
		if (workspaceMember == null || !workspaceMember.isActive() ||
			(!workspaceMember.getPositionType().equals("ADMIN") && !workspaceMember.getPositionType()
				.equals("OWNER"))) {
			throw new CustomException("WORKSPACE_AUTHORIZATION_FAILED", "워크스페이스에 대한 권한이 없습니다.");
		}
		Workspace originalWorkspace = workspaceRepository.findById(id)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
		// 워크스페이스 이름 중복 확인
		if (workspaceRepository.existsByName(workspace.getName()) &&
			!originalWorkspace.getName().equals(workspace.getName())) {
			throw new CustomException("WORKSPACE_NAME_DUPLICATE", "워크스페이스 이름이 중복되었습니다.");
		}
		// 워크스페이스 정보 업데이트
		originalWorkspace.setName(workspace.getName());
		originalWorkspace.setDescription(workspace.getDescription());
		originalWorkspace.setVisibility(workspace.getVisibility());
		originalWorkspace.setUpdatedAt(workspace.getUpdatedAt());
		// 워크스페이스 저장
		return workspaceRepository.save(originalWorkspace);
	}

	public void deleteWorkspace(Long id, Member member) {
		// 워크스페이스 멤버 확인
		WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(id,
			member.getId());
		// 워크스페이스 멤버가 존재하지 않거나 비활성화된 경우, 또는 ADMIN, OWNER가 아닌 경우
		if (workspaceMember == null || !workspaceMember.isActive() ||
			(!workspaceMember.getPositionType().equals("ADMIN") && !workspaceMember.getPositionType()
				.equals("OWNER"))) {
			throw new CustomException("WORKSPACE_AUTHORIZATION_FAILED", "워크스페이스에 대한 권한이 없습니다.");
		}
		// 워크스페이스가 이미 is_deleted 상태인 경우
		if (workspaceRepository.findById(id).orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND",
			"워크스페이스 정보를 찾을 수 없습니다.")).isDeleted()) {
			throw new CustomException("WORKSPACE_ALREADY_DELETED", "이미 삭제된 워크스페이스입니다.");
		}
		// 워크스페이스 삭제
		// is_deleted 컬럼을 true로 업데이트
		Workspace workspace = workspaceRepository.findById(id)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
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
}
