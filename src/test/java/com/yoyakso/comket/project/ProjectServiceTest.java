package com.yoyakso.comket.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.Rollback;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.dto.ProjectCreateRequest;
import com.yoyakso.comket.project.dto.ProjectInfoResponse;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.project.service.ProjectServiceImpl;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import jakarta.transaction.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
@Rollback
public class ProjectServiceTest {

	@InjectMocks
	private ProjectServiceImpl projectService;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private WorkspaceRepository workspaceRepository;

	@Mock
	private FileService fileService;

	@Mock
	private ProjectMemberService projectMemberService;

	@Mock
	private ProjectMemberRepository projectMemberRepository;

	@Mock
	private WorkspaceMemberService workspaceMemberService; // 추가

	@Test
	void testCreateProject() {
		// given
		Long workspaceId = 1L;

		Workspace mockWorkspace = new Workspace();
		mockWorkspace.setId(workspaceId);
		mockWorkspace.setName("Test Workspace");

		ProjectCreateRequest request = new ProjectCreateRequest(
			"COMKET_BE",
			"COMKET Backend Team Project",
			true,
			null
		);

		Project savedProject = Project.builder()
			.id(100L)
			.workspace(mockWorkspace)
			.name(request.getName())
			.description(request.getDescription())
			.isPublic(request.getIsPublic())
			.state(ProjectState.ACTIVE)
			.build();

		Member member = new Member();
		member.setId(1L);

		when(workspaceRepository.findByName("Test Workspace")).thenReturn(Optional.of(mockWorkspace));
		when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

		projectRepository.save(savedProject);

		// when
		ProjectInfoResponse response = projectService.createProject(mockWorkspace.getName(), request, member);

		// then
		assertEquals(100L, response.getProjectId());
		assertEquals("COMKET_BE", response.getProjectName());
	}

	@Test
	void testUpdateProject_authorization_failed() {
		// given
		// mock workspace 설정
		Long workspaceId = 1L;
		Workspace mockWorkspace = new Workspace();
		mockWorkspace.setId(workspaceId);
		mockWorkspace.setName("Test Workspace");

		// member mock
		Member member = new Member();
		member.setId(1L);

		// 업데이트 리쿼스트 mock
		ProjectCreateRequest updateRequest = new ProjectCreateRequest(
			"COMKET_BE",
			"COMKET Backend Team Project",
			true,
			null
		);

		// 이미 존재하는 프로젝트
		Project existingProject = Project.builder()
			.id(100L)
			.workspace(mockWorkspace)
			.name("COMKET_BE")
			.description("Some Descriptions")
			.isPublic(true)
			.state(ProjectState.ACTIVE)
			.build();

		// projectId가 아닌 다른 프로젝트에서 newName이 이미 사용 중인 상황
		when(workspaceRepository.findByName("Test Workspace")).thenReturn(Optional.of(mockWorkspace));

		// when & then
		CustomException thrown = assertThrows(CustomException.class, () ->
			projectService.updateProject(mockWorkspace.getName(), 100L, updateRequest, member)
		);

		assertEquals("PROJECT_AUTHORIZATION_FAILED", thrown.getCode());
	}

	@Test
	void testRequestProject_all() {
		// given
		Long workspaceId = 1L;

		Workspace mockWorkspace = new Workspace();
		mockWorkspace.setId(workspaceId);
		mockWorkspace.setName("Test Workspace");

		Project savedProject1 = Project.builder()
			.id(100L)
			.workspace(mockWorkspace)
			.name("Project1")
			.description("")
			.isPublic(true)
			.state(ProjectState.ACTIVE)
			.build();

		Project savedProject2 = Project.builder()
			.id(101L)
			.workspace(mockWorkspace)
			.name("Project2")
			.description("")
			.isPublic(true)
			.state(ProjectState.ACTIVE)
			.build();

		Project savedProject3 = Project.builder()
			.id(102L)
			.workspace(mockWorkspace)
			.name("Project3")
			.description("")
			.isPublic(true)
			.state(ProjectState.ACTIVE)
			.build();

		Member member = new Member();
		member.setId(1L);

		WorkspaceMember mockWorkspaceMember = WorkspaceMember.builder()
			.id(1L)
			.workspace(mockWorkspace)
			.member(member)
			.state(WorkspaceMemberState.ACTIVE)
			.positionType("Developer")
			.build();

		when(workspaceRepository.findByName("Test Workspace")).thenReturn(Optional.of(mockWorkspace));
		when(workspaceMemberService.getWorkspaceMemberById(mockWorkspaceMember.getId()))
			.thenReturn(mockWorkspaceMember);
		when(projectRepository.findAllByWorkspaceAndIsPublicTrue(mockWorkspace))
			.thenReturn(List.of(savedProject1, savedProject2, savedProject3));
		// when
		List<ProjectInfoResponse> response = projectService.getAllProjects(mockWorkspace.getName(), member);

		// then
		assertEquals(3, response.size());
		assertEquals("Project1", response.get(0).getProjectName());
		assertEquals("Project2", response.get(1).getProjectName());
		assertEquals("Project3", response.get(2).getProjectName());
	}
}
