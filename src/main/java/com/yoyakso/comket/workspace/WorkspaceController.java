package com.yoyakso.comket.workspace;

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

import com.yoyakso.comket.member.MemberService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.dto.WorkspaceInfoResponse;
import com.yoyakso.comket.workspace.dto.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.dto.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.entity.Workspace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
	private final WorkspaceService workspaceService;
	private final MemberService memberService;

	// 워크스페이스 생성
	@Operation(
		summary = "워크스페이스 생성 API",
		description = "워크스페이스를 생성하는 API",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "워크스페이스 생성 요청 정보",
			required = true,
			content = @io.swagger.v3.oas.annotations.media.Content(
				schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = WorkspaceRegisterRequest.class)
			)
		)
	)
	@PostMapping
	public ResponseEntity<WorkspaceInfoResponse> createWorkspace(
		@RequestBody WorkspaceRegisterRequest workspaceRegisterRequest, HttpServletRequest request) {
		// 인증된 회원 정보 가져오기
		Member authenticatedMember = memberService.getAuthenticatedMember(request);
		if (authenticatedMember == null) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}
		// 워크스페이스 등록 요청을 Workspace 엔티티로 변환
		Workspace workspace = Workspace.fromRequest(workspaceRegisterRequest);
		Workspace createdWorkspace = workspaceService.registerWorkspace(workspace, authenticatedMember);
		return ResponseEntity.ok(
			WorkspaceInfoResponse.builder()
				.id(createdWorkspace.getId())
				.name(createdWorkspace.getName())
				.description(createdWorkspace.getDescription())
				.visibility(createdWorkspace.getVisibility())
				.createdAt(createdWorkspace.getCreatedAt().toString())
				.updatedAt(createdWorkspace.getUpdatedAt().toString())
				.build()
		);
	}

	// 워크스페이스 목록 조회
	// 자신이 속해 있는 워크스페이스 목록 조회, 전체 조회
	@GetMapping
	@Operation(
		summary = "워크스페이스 목록 조회 API",
		description = "자신이 속한 워크스페이스 목록을 조회하거나, 공개된 워크스페이스를 포함하여 조회할 수 있는 API",
		parameters = {
			@Parameter(name = "includePublic", description = "공개된 워크스페이스를 포함할지 여부", required = false, example = "true")
		}
	)
	public ResponseEntity<List<WorkspaceInfoResponse>> getWorkspaces(HttpServletRequest request,
		@RequestParam(defaultValue = "false") boolean includePublic) {
		// 인증된 회원 정보 가져오기
		Member authenticatedMember = memberService.getAuthenticatedMember(request);
		if (authenticatedMember == null) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}
		// 인증된 회원의 워크스페이스 목록 조회
		List<Workspace> workspaces = includePublic ? workspaceService.getPublicWorkspaces() :
			workspaceService.getWorkspacesByMember(authenticatedMember);

		// 워크스페이스 정보를 WorkspaceInfoResponse DTO로 변환
		return ResponseEntity.ok(
			workspaces.stream()
				.map(workspace -> WorkspaceInfoResponse.builder()
					.id(workspace.getId())
					.name(workspace.getName())
					.description(workspace.getDescription())
					.visibility(workspace.getVisibility())
					.createdAt(workspace.getCreatedAt().toString())
					.updatedAt(workspace.getUpdatedAt().toString())
					.build()
				)
				.toList()
		);
	}

	// 워크스페이스 단건 조회
	@Operation(
		summary = "워크스페이스 단건 조회 API",
		description = "워크스페이스의 정보를 조회하는 API",
		parameters = {
			@Parameter(name = "id", description = "조회할 워크스페이스 ID", required = true)
		}
	)
	@GetMapping("/{id}")
	public ResponseEntity<WorkspaceInfoResponse> getWorkspaceById(HttpServletRequest request,
		@PathVariable Long id) {
		// 인증된 회원 정보 가져오기
		Member authenticatedMember = memberService.getAuthenticatedMember(request);
		if (authenticatedMember == null) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}
		Workspace workspace = workspaceService.getWorkspaceById(id, authenticatedMember);
		if (workspace == null) {
			return ResponseEntity.notFound().build(); // 워크스페이스가 존재하지 않거나 비공개인 경우
		}
		return ResponseEntity.ok(
			WorkspaceInfoResponse.builder()
				.id(workspace.getId())
				.name(workspace.getName())
				.description(workspace.getDescription())
				.visibility(workspace.getVisibility())
				.createdAt(workspace.getCreatedAt().toString())
				.updatedAt(workspace.getUpdatedAt().toString())
				.build()
		);
	}

	// 워크스페이스 수정
	@Operation(
		summary = "워크스페이스 수정 API",
		description = "워크스페이스의 정보를 수정하는 API",
		parameters = {
			@Parameter(name = "id", description = "수정할 워크스페이스 ID", required = true)
		}
	)
	@PatchMapping("/{id}")
	public ResponseEntity<WorkspaceInfoResponse> updateWorkspace(@PathVariable Long id,
		@RequestBody WorkspaceUpdateRequest workspaceUpdateRequest, HttpServletRequest request) {
		// 인증된 회원 정보 가져오기
		Member authenticatedMember = memberService.getAuthenticatedMember(request);
		if (authenticatedMember == null) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}
		// 워크스페이스 수정 진행
		Workspace updatedWorkspace = workspaceService.updateWorkspace(authenticatedMember, id,
			Workspace.fromRequest(workspaceUpdateRequest));
		// 워크스페이스 수정 후 응답
		return ResponseEntity.ok(
			WorkspaceInfoResponse.builder()
				.id(updatedWorkspace.getId())
				.name(updatedWorkspace.getName())
				.description(updatedWorkspace.getDescription())
				.visibility(updatedWorkspace.getVisibility())
				.createdAt(updatedWorkspace.getCreatedAt().toString())
				.updatedAt(updatedWorkspace.getUpdatedAt().toString())
				.build()
		);
	}

	@Operation(
		summary = "워크스페이스 삭제 API",
		description = "워크스페이스를 삭제하는 API",
		parameters = {
			@Parameter(name = "id", description = "삭제할 워크스페이스 ID", required = true)
		}
	)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id, HttpServletRequest request) {
		// 인증된 회원 정보 가져오기
		Member authenticatedMember = memberService.getAuthenticatedMember(request);
		if (authenticatedMember == null) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}

		// 워크스페이스 삭제 진행
		workspaceService.deleteWorkspace(id, authenticatedMember);

		// 삭제 완료 응답
		return ResponseEntity.noContent().build(); // 204 No Content
	}
}
