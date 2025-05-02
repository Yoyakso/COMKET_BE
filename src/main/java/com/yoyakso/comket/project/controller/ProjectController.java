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

import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class ProjectController {
	private final ProjectService projectService;

	@Operation(method = "POST", description = "프로젝트 생성 API")
	@PostMapping("/{workspaceName}/project")
	public ResponseEntity<ProjectInfoResponse> createProject(
		@PathVariable("workspaceName") String workspaceName,
		@RequestBody ProjectCreateRequest request,
		HttpServletRequest userRequest
	) {
		ProjectInfoResponse info = projectService.createProject(workspaceName, request, userRequest);
		return ResponseEntity.ok(info);
	}

	@Operation(method = "PATCH", description = "프로젝트 수정 API")
	@PatchMapping("/{workspaceName}/{projectId}")
	public ResponseEntity<ProjectInfoResponse> updateProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId,
		@RequestBody ProjectCreateRequest request,
		HttpServletRequest userRequest
	) {
		ProjectInfoResponse info = projectService.patchProject(workspaceName, projectId, request, userRequest);
		return ResponseEntity.ok(info);
	}

	@Operation(method = "DELETE", description = "프로젝트 삭제 API")
	@DeleteMapping("/{workspaceName}/{projectId}")
	public ResponseEntity<Void> deleteProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId,
		HttpServletRequest userRequest
	) {
		projectService.deleteProject(workspaceName, projectId, userRequest);
		return ResponseEntity.noContent().build();
	}

	@Operation(method = "DELETE", description = "프로젝트 회원 탈퇴 API")
	@DeleteMapping("/{workspaceName}/{projectId}/member")
	public ResponseEntity<Void> exitProject(
		@PathVariable("workspaceName") String workspaceName,
		@PathVariable("projectId") Long projectId,
		HttpServletRequest userRequest
	) {
		projectService.exitProject(workspaceName, projectId, userRequest);
		return ResponseEntity.noContent().build();
	}

	@Operation(method = "GET", description = "프로젝트 전체 조회 API")
	@GetMapping("/{workspaceName}/projects")
	public ResponseEntity<List<ProjectInfoResponse>> getProjects(
		@PathVariable("workspaceName") String workspaceName,
		HttpServletRequest userRequest
	) {
		List<ProjectInfoResponse> responses = projectService.getAllProjects(workspaceName, userRequest);
		return ResponseEntity.ok(responses);
	}

	@Operation(method = "GET", description = "내가 속한 프로젝트 조회 API")
	@GetMapping("/{workspaceName}/projects/member")
	public ResponseEntity<List<ProjectInfoResponse>> getAllIncludeProjects(
		@PathVariable("workspaceName") String workspaceName,
		HttpServletRequest userRequest
	) {
		List<ProjectInfoResponse> responses = projectService.getAllProjectsByMember(workspaceName, userRequest);
		return ResponseEntity.ok(responses);
	}
}
