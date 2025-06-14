package com.yoyakso.comket.workspace.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.dto.request.WorkspaceMemberAuthorityUpdateRequest;
import com.yoyakso.comket.workspace.dto.request.WorkspaceMemberCreateRequest;
import com.yoyakso.comket.workspace.dto.request.WorkspaceMemberInfoUpdateRequest;
import com.yoyakso.comket.workspace.dto.request.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.dto.request.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.dto.response.WorkspaceInfoResponse;
import com.yoyakso.comket.workspace.dto.response.WorkspaceMemberInfoResponse;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

	private final WorkspaceService workspaceService;
	private final WorkspaceMemberService workspaceMemberService;
	private final MemberService memberService;

	@PostMapping
	@Operation(summary = "워크스페이스 생성 API", description = "워크스페이스를 생성하는 API")
	public ResponseEntity<WorkspaceInfoResponse> createWorkspace(
		@Valid @RequestBody WorkspaceRegisterRequest workspaceRegisterRequest) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		Workspace createdWorkspace = workspaceService.registerWorkspace(workspaceRegisterRequest, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(createdWorkspace));
	}

	@GetMapping
	@Operation(summary = "워크스페이스 목록 조회 API", description = "자신이 속한 워크스페이스 목록을 조회하거나 공개된 워크스페이스를 포함하여 조회")
	public ResponseEntity<List<WorkspaceInfoResponse>> getWorkspaces(
		@RequestParam(defaultValue = "false") boolean includePublic
	) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		List<Workspace> workspaces = includePublic
			? workspaceService.getPublicWorkspaces()
			: workspaceService.getWorkspacesByMember(authenticatedMember);
		return ResponseEntity.ok(workspaces.stream().map(workspaceService::toResponse).toList());
	}

	@GetMapping("/{id}")
	@Operation(summary = "워크스페이스 단건 조회 API", description = "워크스페이스의 정보를 조회하는 API")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceById(@PathVariable Long id) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		Workspace workspace = workspaceService.getWorkspaceById(id, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(workspace));
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "워크스페이스 슬러그 조회 API", description = "워크스페이스의 슬러그를 통해 워크스페이스 정보를 조회하는 API")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceBySlug(@PathVariable String slug) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		Workspace workspace = workspaceService.getWorkspaceBySlug(slug, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(workspace));
	}

	@GetMapping("/inviteCode/{inviteCode}")
	@Operation(summary = "워크스페이스 초대 코드 조회 API", description = "워크스페이스의 초대 코드를 통해 워크스페이스 정보를 조회하는 API")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceByInviteCode(@PathVariable String inviteCode) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		Workspace workspace = workspaceService.getWorkspaceByInviteCode(inviteCode, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(workspace));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "워크스페이스 수정 API", description = "워크스페이스의 정보를 수정하는 API")
	public ResponseEntity<WorkspaceInfoResponse> updateWorkspace(
		@PathVariable Long id,
		@Valid @RequestBody WorkspaceUpdateRequest workspaceUpdateRequest
	) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		Workspace updatedWorkspace = workspaceService.updateWorkspace(authenticatedMember, id, workspaceUpdateRequest);
		return ResponseEntity.ok(workspaceService.toResponse(updatedWorkspace));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "워크스페이스 삭제 API", description = "워크스페이스를 삭제하는 API")
	public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		workspaceService.deleteWorkspace(id, authenticatedMember);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/members")
	@Operation(summary = "워크스페이스 멤버 초대 API", description = "워크스페이스에 멤버를 초대하는 API")
	public ResponseEntity<List<WorkspaceMemberInfoResponse>> inviteWorkspaceMember(
		@PathVariable Long id,
		@Valid @RequestBody WorkspaceMemberCreateRequest workspaceMemberCreateRequest
	) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		List<WorkspaceMemberInfoResponse> invitedWorkspaceMemberInfo = workspaceService.inviteWorkspaceMember(id,
			workspaceMemberCreateRequest, authenticatedMember);
		return ResponseEntity.ok(invitedWorkspaceMemberInfo);
	}

	@GetMapping("/{id}/members")
	@Operation(summary = "워크스페이스 멤버 조회 API", description = "워크스페이스의 멤버를 조회하는 API")
	public ResponseEntity<List<WorkspaceMemberInfoResponse>> getWorkspaceMembers(
		@PathVariable Long id,
		@RequestParam(required = false) List<String> positionTypes, // 여러 포지션 필터
		@RequestParam(required = false) List<String> memberStates, // 여러 멤버 상태 필터
		@RequestParam(required = false) String keyword // 검색 키워드
	) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		List<WorkspaceMember> workspaceMembers = workspaceService.getWorkspaceMembers(id, authenticatedMember,
			positionTypes, memberStates, keyword);
		return ResponseEntity.ok(workspaceMembers.stream().map(workspaceService::toMemberInfoResponse).toList());
	}

	@GetMapping("/{workspaceId}/member")
	@Operation(summary = "워크스페이스 멤버 자기 자신 조회 API", description = "워크스페이스의 자기 프로필 정보를 조회하는 API")
	public ResponseEntity<WorkspaceMemberInfoResponse> getWorkspaceMember(
		@PathVariable Long workspaceId
	) {
		Member authenticatedMember = memberService.getAuthenticatedMember();
		WorkspaceMember workspaceMember = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(workspaceId,
			authenticatedMember.getId());
		return ResponseEntity.ok(workspaceService.toMemberInfoResponse(workspaceMember));
	}

	@PatchMapping("/{workspaceId}/members")
	@Operation(summary = "워크스페이스 멤버 권한 수정 API", description = "워크스페이스의 멤버 정보를 수정하는 API")
	public ResponseEntity<WorkspaceMemberInfoResponse> updateWorkspaceMember(
		@PathVariable Long workspaceId,
		@Valid @RequestBody WorkspaceMemberAuthorityUpdateRequest workspaceMemberInfoUpdateRequest
	) {
		Member controlMember = memberService.getAuthenticatedMember();
		WorkspaceMember updatedWorkspaceMember = workspaceService.updateWorkspaceMemberAuthority(workspaceId,
			workspaceMemberInfoUpdateRequest, controlMember);
		return ResponseEntity.ok(workspaceService.toMemberInfoResponse(updatedWorkspaceMember));
	}

	@PatchMapping("/{workspaceId}/members/info")
	@Operation(summary = "워크스페이스 멤버 정보 수정 API", description = "워크스페이스의 멤버 정보를 수정하는 API")
	public ResponseEntity<WorkspaceMemberInfoResponse> updateWorkspaceMemberInfo(
		@PathVariable Long workspaceId,
		@Valid @RequestBody WorkspaceMemberInfoUpdateRequest workspaceMemberInfoUpdateRequest
	) {
		Member member = memberService.getAuthenticatedMember();
		WorkspaceMember updatedWorkspaceMember = workspaceService.updateWorkspaceMemberInfo(workspaceId,
			workspaceMemberInfoUpdateRequest, member);
		return ResponseEntity.ok(workspaceService.toMemberInfoResponse(updatedWorkspaceMember));
	}

	@DeleteMapping("/{workspaceId}/members")
	@Operation(summary = "워크스페이스 멤버 삭제 API", description = "워크스페이스의 멤버를 삭제하는 API")
	public ResponseEntity<Void> deleteWorkspaceMember(
		@PathVariable Long workspaceId,
		@RequestParam String targetMemberEmail
	) {
		Member controllMember = memberService.getAuthenticatedMember();
		Member targetMember = memberService.getMemberByEmail(targetMemberEmail);
		workspaceService.deleteWorkspaceMember(workspaceId, controllMember, targetMember);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/leave-all")
	@Operation(summary = "모든 워크스페이스 탈퇴 API", description = "자신이 소속된 모든 워크스페이스에서 탈퇴하는 API")
	public ResponseEntity<Void> leaveAllWorkspaces() {
		Member member = memberService.getAuthenticatedMember();
		workspaceService.leaveAllWorkspaces(member);
		return ResponseEntity.noContent().build();
	}
}
