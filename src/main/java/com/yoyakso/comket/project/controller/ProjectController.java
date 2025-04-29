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

	// CRUD
	/*
	NAME: 프로젝트명
	Desc: 설명
	Profile_Url: 이미지 URl(S3)

	is_deleted: Soft delete
	Created_at: 생성 시간
	Updated_at: 업데이트 시간
	 */

	@Operation(method = "POST", description = "프로젝트 생성 API")
	@PostMapping("/{workspaceId}/project")
	public ResponseEntity<ProjectInfoResponse> createProject(
		@PathVariable("workspaceId") Long workspaceId,
		@RequestBody ProjectCreateRequest request
	) {
		// service 연결
		// TODO: 프로젝트 생성 시 생성자는 자동으로 프로젝트에 추가?
		ProjectInfoResponse info = projectService.createProject(workspaceId, request);

		return ResponseEntity.ok(info);
	}

	@Operation(method = "")
}
