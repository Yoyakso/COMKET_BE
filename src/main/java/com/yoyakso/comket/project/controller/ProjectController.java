package com.yoyakso.comket.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.dto.ProjectMemberInviteRequest;
import com.yoyakso.comket.project.dto.ProjectMemberResponse;
import com.yoyakso.comket.project.dto.ProjectMemberUpdateRequest;
import com.yoyakso.comket.project.dto.ProjectOwnerTransferRequest;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class ProjectController {
	private final ProjectService projectService;
	private final MemberService memberService;

	// POST
	@Operation(method = "POST", description = "프로젝트 생성 API")
	@PostMapping("/{workspaceName}/project")
	public ResponseEntity<ProjectInfoResponse> createProject(
		@PathVariable("workspaceName") String workspaceName,
		@Valid @RequestBody ProjectCreateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		ProjectInfoResponse info = projectService.createProject(workspaceName, request, member);
		return ResponseEntity.ok(info);
	}

	@Operation(method = "POST", description = "프로젝트 멤버 추가 API")
	@PostMapping("/{workspaceName}/{projectId}/members")
	public ResponseEntity<List<ProjectMemberResponse>> inviteProjectMember(
		@PathVariable String workspaceName,
		@PathVariable Long projectId,
		@Valid @RequestBody ProjectMemberInviteRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		List<ProjectMemberResponse> invitedProjectMemberInfo = projectService.inviteProjectMembers(
			workspaceName,
			projectId,
			member,
			request
		);

		return ResponseEntity.ok(invitedProjectMemberInfo);
	}

	// GET
	// 유저의 포지션에 따라 다 보이거나 퍼블릭만 보임
	@Operation(method = "GET", description = "프로젝트 전체 조회 API")
	@GetMapping("/{workspaceName}/project/all")
	public ResponseEntity<List<ProjectInfoResponse>> getProjects(
		@PathVariable("workspaceName") String workspaceName
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		List<ProjectInfoResponse> responses = projectService.getAllProjects(workspaceName, member);
		return ResponseEntity.ok(responses);
	}

	@Operation(method = "GET", description = "프로젝트 단건 조회 API")
	@GetMapping("/{workspaceName}/{projectId}")
	public ResponseEntity<ProjectInfoResponse> getProjects(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		ProjectInfoResponse responses = projectService.getProject(workspaceName, projectId, member);
		return ResponseEntity.ok(responses);
	}

	@Operation(method = "GET", description = "내가 속한 프로젝트 조회 API")
	@GetMapping("/{workspaceName}/project/my")
	public ResponseEntity<List<ProjectInfoResponse>> getAllIncludeProjects(
		@PathVariable("workspaceName") String workspaceName
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		List<ProjectInfoResponse> responses = projectService.getAllProjectsByMember(workspaceName, member);
		return ResponseEntity.ok(responses);
	}

	@Operation(method = "GET", description = "프로젝트 멤버 전체 조회")
	@GetMapping("/{workspaceName}/{projectId}/members")
	public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
		@PathVariable String workspaceName,
		@PathVariable Long projectId
	) {
		if (memberService.getAuthenticatedMember() == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		List<ProjectMemberResponse> responses = projectService.getProjectMembers(workspaceName, projectId);
		return ResponseEntity.ok(responses);
	}

	// PATCH
	@Operation(method = "PATCH", description = "프로젝트 수정 API")
	@PatchMapping("/{workspaceName}/{projectId}/edit")
	public ResponseEntity<ProjectInfoResponse> updateProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId,
		@Valid @RequestBody ProjectCreateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		ProjectInfoResponse info = projectService.updateProject(workspaceName, projectId, request, member);
		return ResponseEntity.ok(info);
	}

	@Operation(method = "PATCH", description = "프로젝트 비활성화 API")
	@PatchMapping("/{workspaceName}/{projectId}/inactive")
	public ResponseEntity<Void> inActiveProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		projectService.patchProjectState(workspaceName, projectId, member, ProjectState.INACTIVE);
		return ResponseEntity.noContent().build();
	}

	@Operation(method = "PATCH", description = "프로젝트 멤버 관리")
	@PatchMapping("/{workspaceName}/{projectId}/edit/members")
	public ResponseEntity<ProjectMemberResponse> patchProjectMembers(
		@PathVariable String workspaceName,
		@PathVariable Long projectId,
		@Valid @RequestBody ProjectMemberUpdateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		ProjectMemberResponse responses = projectService.patchProjectMembersPosition(workspaceName, projectId, member,
			request);
		return ResponseEntity.ok(responses);
	}

	@Operation(method = "PATCH", description = "프로젝트 소유자 권한 이전")
	@PatchMapping("/{workspaceName}/{projectId}/edit/owner")
	public ResponseEntity<ProjectMemberResponse> patchProjectOwners(
		@PathVariable String workspaceName,
		@PathVariable Long projectId,
		@Valid @RequestBody ProjectOwnerTransferRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		ProjectMemberResponse responses = projectService.assignNewOwner(workspaceName, projectId, member,
			request.getTargetMemberId());
		return ResponseEntity.ok(responses);
	}

	// DELETE
	@Operation(method = "DELETE", description = "프로젝트 삭제 API")
	@DeleteMapping("/{workspaceName}/{projectId}")
	public ResponseEntity<Void> deleteProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		projectService.patchProjectState(workspaceName, projectId, member, ProjectState.DELETED);
		return ResponseEntity.noContent().build();
	}

	@Operation(method = "DELETE", description = "프로젝트 회원 탈퇴 API")
	@DeleteMapping("/{workspaceName}/{projectId}/exit")
	public ResponseEntity<Void> exitProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		projectService.exitProject(workspaceName, projectId, member);
		return ResponseEntity.noContent().build();
	}

	@Operation(method = "DELETE", description = "프로젝트 멤버 삭제")
	@DeleteMapping("/{workspaceName}/{projectId}/edit/members")
	public ResponseEntity<Void> deleteProjectMembers(
		@PathVariable String workspaceName,
		@PathVariable Long projectId,
		Long projectMemberId
	) {
		Member member = memberService.getAuthenticatedMember();
		if (member == null) {
			throw new CustomException("MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
		}
		projectService.deleteProjectMember(workspaceName, projectId, member, projectMemberId);

		return ResponseEntity.noContent().build();
	}
}
