package com.yoyakso.comket.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class ProjectController {
	private final ProjectService projectService;

	@Operation(method = "POST", description = "프로젝트 생성 API")
	@PostMapping("/{workspaceId}/project")
	public ResponseEntity<ProjectInfoResponse> createProject(
		@PathVariable("workspaceId") Long workspaceId,
		@RequestBody ProjectCreateRequest request
	) {
		ProjectInfoResponse info = projectService.createProject(workspaceId, request);

		return ResponseEntity.ok(info);
	}
}
