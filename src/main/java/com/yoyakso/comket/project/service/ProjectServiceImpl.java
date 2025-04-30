package com.yoyakso.comket.project.service;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
	private final ProjectRepository projectRepository;
	private final WorkspaceRepository workspaceRepository;

	@Override
	@Transactional
	public ProjectInfoResponse createProject(Long workspaceId, ProjectCreateRequest request) {

		Workspace workSpace = workspaceRepository.findById(workspaceId)
			.orElseThrow(() -> new CustomException("WORKSPACE_NOT_FOUND", "워크스페이스를 찾을 수 없습니다."));

		Project project = Project.builder()
			.workspace(workSpace)
			.name(request.getName())
			.purpose(request.getPurpose())
			.description(request.getDescription())
			.state(ProjectState.ACTIVE) // 초기 상태 예: ACTIVE
			.isPublic(request.getIsPublic())
			.build();

		projectRepository.save(project);

		return ProjectInfoResponse.builder()
			.projectId(project.getId())
			.projectName(project.getName())
			.build();
	}
}
