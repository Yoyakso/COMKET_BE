package com.yoyakso.comket.workspace;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.workspace.entity.Workspace;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
	private final WorkspaceRepository workspaceRepository;

	public void save(Workspace workspace) {
		if (workspace.getId() == null) {
			workspaceRepository.save(workspace);
		} else {
			Workspace existingWorkspace = workspaceRepository.findById(workspace.getId())
				.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
			existingWorkspace.setName(workspace.getName());
			existingWorkspace.setDescription(workspace.getDescription());
			workspaceRepository.save(existingWorkspace);
		}
	}

	public void deleteWorkspace(Long id) {
		Workspace workspace = workspaceRepository.findById(id)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
		workspaceRepository.delete(workspace);
	}

	public Workspace createWorkspace(Workspace workspace) {
		return workspaceRepository.save(workspace);
	}

	public Workspace getWorkspaceById(Long id) {
		return workspaceRepository.findById(id)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
	}

	public List<Workspace> getAllWorkspaces() {
		return workspaceRepository.findAll();
	}

	public Workspace updateWorkspace(Long id, Workspace workspace) {
		Workspace existingWorkspace = workspaceRepository.findById(id)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스 정보를 찾을 수 없습니다."));
		existingWorkspace.setName(workspace.getName());
		existingWorkspace.setDescription(workspace.getDescription());
		return workspaceRepository.save(existingWorkspace);
	}
}
