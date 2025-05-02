package com.yoyakso.comket.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;

import jakarta.servlet.http.HttpServletRequest;

@Service
public interface ProjectService {
	ProjectInfoResponse createProject(String workSpaceName, ProjectCreateRequest request,
		HttpServletRequest userRequest);

	ProjectInfoResponse patchProject(String workSpaceName, Long projectId, ProjectCreateRequest request,
		HttpServletRequest userRequest);

	void deleteProject(String workSpaceName, Long projectId, HttpServletRequest userRequest);

	void exitProject(String workSpaceName, Long projectId, HttpServletRequest userRequest);

	List<ProjectInfoResponse> getAllProjects(String workSpaceName, HttpServletRequest userRequest);

	List<ProjectInfoResponse> getAllProjectsByMember(String workSpaceName, HttpServletRequest userRequest);
}
