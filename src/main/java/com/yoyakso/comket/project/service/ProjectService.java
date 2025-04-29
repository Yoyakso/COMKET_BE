package com.yoyakso.comket.project.service;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;

@Service
public interface ProjectService {
	ProjectInfoResponse createProject(Long workspaceId, ProjectCreateRequest request);
}
