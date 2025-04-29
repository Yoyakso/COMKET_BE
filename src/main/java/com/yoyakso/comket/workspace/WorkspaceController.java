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
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.workspace.entity.Workspace;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
	private final WorkspaceService workspaceService;

	public WorkspaceController(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	// 워크스페이스 생성
	@PostMapping
	public ResponseEntity<Workspace> createWorkspace(@RequestBody Workspace workspace) {
		Workspace createdWorkspace = workspaceService.createWorkspace(workspace);
		return ResponseEntity.ok(createdWorkspace);
	}

	// 워크스페이스 목록 조회
	@GetMapping
	public ResponseEntity<List<Workspace>> getAllWorkspaces() {
		List<Workspace> workspaces = workspaceService.getAllWorkspaces();
		return ResponseEntity.ok(workspaces);
	}

	// 워크스페이스 단건 조회
	@GetMapping("/{id}")
	public ResponseEntity<Workspace> getWorkspaceById(@PathVariable Long id) {
		Workspace workspace = workspaceService.getWorkspaceById(id);
		return ResponseEntity.ok(workspace);
	}

	// 워크스페이스 수정
	@PatchMapping("/{id}")
	public ResponseEntity<Workspace> updateWorkspace(@PathVariable Long id, @RequestBody Workspace workspace) {
		Workspace updatedWorkspace = workspaceService.updateWorkspace(id, workspace);
		return ResponseEntity.ok(updatedWorkspace);
	}

	// 워크스페이스 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
		workspaceService.deleteWorkspace(id);
		return ResponseEntity.noContent().build();
	}
}
