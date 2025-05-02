package com.yoyakso.comket.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;

@Service
public interface ProjectService {
	ProjectInfoResponse createProject(String workSpaceName, ProjectCreateRequest request,
		Member member);

	ProjectInfoResponse updateProject(String workSpaceName, Long projectId, ProjectCreateRequest request,
		Member member);

	void deleteProject(String workSpaceName, Long projectId, Member member);

	void exitProject(String workSpaceName, Long projectId, Member member);

	List<ProjectInfoResponse> getAllProjects(String workSpaceName, Member member);

	List<ProjectInfoResponse> getAllProjectsByMember(String workSpaceName, Member member);
}
