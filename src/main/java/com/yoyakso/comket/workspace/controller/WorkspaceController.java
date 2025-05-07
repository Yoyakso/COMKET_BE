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

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.dto.WorkspaceInfoResponse;
import com.yoyakso.comket.workspace.dto.WorkspaceMemberInfoResponse;
import com.yoyakso.comket.workspace.dto.WorkspaceMemberInfoUpdateRequest;
import com.yoyakso.comket.workspace.dto.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.dto.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

	private final WorkspaceService workspaceService;
	private final MemberService memberService;

	@PostMapping
	@Operation(summary = "워크스페이스 생성 API", description = "워크스페이스를 생성하는 API")
	public ResponseEntity<WorkspaceInfoResponse> createWorkspace(
		@Valid @RequestBody WorkspaceRegisterRequest workspaceRegisterRequest, HttpServletRequest request) {
		Member authenticatedMember = getAuthenticatedMember(request);
		Workspace createdWorkspace = workspaceService.registerWorkspace(workspaceRegisterRequest, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(createdWorkspace));
	}

	@GetMapping
	@Operation(summary = "워크스페이스 목록 조회 API", description = "자신이 속한 워크스페이스 목록을 조회하거나 공개된 워크스페이스를 포함하여 조회")
	public ResponseEntity<List<WorkspaceInfoResponse>> getWorkspaces(HttpServletRequest request,
		@RequestParam(defaultValue = "false") boolean includePublic) {
		Member authenticatedMember = getAuthenticatedMember(request);
		List<Workspace> workspaces = includePublic
			? workspaceService.getPublicWorkspaces()
			: workspaceService.getWorkspacesByMember(authenticatedMember);
		return ResponseEntity.ok(workspaces.stream().map(workspaceService::toResponse).toList());
	}

	@GetMapping("/{id}")
	@Operation(summary = "워크스페이스 단건 조회 API", description = "워크스페이스의 정보를 조회하는 API")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceById(HttpServletRequest request, @PathVariable Long id) {
		Member authenticatedMember = getAuthenticatedMember(request);
		Workspace workspace = workspaceService.getWorkspaceById(id, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(workspace));
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "워크스페이스 슬러그 조회 API", description = "워크스페이스의 슬러그를 통해 워크스페이스 정보를 조회하는 API")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceBySlug(HttpServletRequest request,
		@PathVariable String slug) {
		Member authenticatedMember = getAuthenticatedMember(request);
		Workspace workspace = workspaceService.getWorkspaceBySlug(slug, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(workspace));
	}

	@GetMapping("/inviteCode/{inviteCode}")
	@Operation(summary = "워크스페이스 초대 코드 조회 API", description = "워크스페이스의 초대 코드를 통해 워크스페이스 정보를 조회하는 API")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceByInviteCode(HttpServletRequest request,
		@PathVariable String inviteCode) {
		Member authenticatedMember = getAuthenticatedMember(request);
		Workspace workspace = workspaceService.getWorkspaceByInviteCode(inviteCode, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toResponse(workspace));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "워크스페이스 수정 API", description = "워크스페이스의 정보를 수정하는 API")
	public ResponseEntity<WorkspaceInfoResponse> updateWorkspace(@PathVariable Long id,
		@Valid @RequestBody WorkspaceUpdateRequest workspaceUpdateRequest,
		HttpServletRequest request) {
		Member authenticatedMember = getAuthenticatedMember(request);
		Workspace updatedWorkspace = workspaceService.updateWorkspace(authenticatedMember, id, workspaceUpdateRequest);
		return ResponseEntity.ok(workspaceService.toResponse(updatedWorkspace));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "워크스페이스 삭제 API", description = "워크스페이스를 삭제하는 API")
	public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id, HttpServletRequest request) {
		Member authenticatedMember = getAuthenticatedMember(request);
		workspaceService.deleteWorkspace(id, authenticatedMember);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/members")
	@Operation(summary = "워크스페이스 멤버 조회 API", description = "워크스페이스의 멤버를 조회하는 API")
	public ResponseEntity<List<WorkspaceMemberInfoResponse>> getWorkspaceMembers(
		@PathVariable Long id, HttpServletRequest request,
		@RequestParam(required = false) List<String> positionTypes, // 여러 포지션 필터
		@RequestParam(required = false) List<String> memberStates, // 여러 멤버 상태 필터
		@RequestParam(required = false) String keyword // 검색 키워드
	) {
		Member authenticatedMember = getAuthenticatedMember(request);
		List<WorkspaceMember> workspaceMembers = workspaceService.getWorkspaceMembers(id, authenticatedMember,
			positionTypes, memberStates, keyword);
		return ResponseEntity.ok(workspaceMembers.stream().map(workspaceService::toMemberInfoResponse).toList());
	}

	@PatchMapping("/{workspaceId}/members")
	@Operation(summary = "워크스페이스 멤버 수정 API", description = "워크스페이스의 멤버 정보를 수정하는 API")
	public ResponseEntity<WorkspaceMemberInfoResponse> updateWorkspaceMember(
		@PathVariable Long workspaceId,
		@Valid @RequestBody WorkspaceMemberInfoUpdateRequest workspaceMemberInfoUpdateRequest,
		HttpServletRequest request) {
		Member authenticatedMember = getAuthenticatedMember(request);
		WorkspaceMember updatedWorkspaceMember = workspaceService.updateWorkspaceMember(workspaceId,
			workspaceMemberInfoUpdateRequest, authenticatedMember);
		return ResponseEntity.ok(workspaceService.toMemberInfoResponse(updatedWorkspaceMember));
	}

	@DeleteMapping("/{workspaceId}/members/{workspaceMemberId}")
	@Operation(summary = "워크스페이스 멤버 삭제 API", description = "워크스페이스의 멤버를 삭제하는 API")
	public ResponseEntity<Void> deleteWorkspaceMember(@PathVariable Long workspaceId,
		@PathVariable Long workspaceMemberId, HttpServletRequest request) {
		Member authenticatedMember = getAuthenticatedMember(request);
		workspaceService.deleteWorkspaceMember(workspaceId, workspaceMemberId, authenticatedMember);
		return ResponseEntity.noContent().build();
	}
	// --- Private Helper Methods ---

	private Member getAuthenticatedMember(HttpServletRequest request) {
		Member member = memberService.getAuthenticatedMember(request);
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		return member;
	}

}