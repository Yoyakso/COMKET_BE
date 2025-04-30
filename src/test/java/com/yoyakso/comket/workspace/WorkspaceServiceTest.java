package com.yoyakso.comket.workspace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.dto.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.Visibility;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

class WorkspaceServiceTest {
	@Mock
	private WorkspaceRepository workspaceRepository;

	@Mock
	private WorkspaceMemberService workspaceMemberService; // Mock 선언

	@InjectMocks
	private WorkspaceService workspaceService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this); // Mockito 초기화
	}

	@Test
	void testRegisterWorkspace() {
		// given
		String name = "New Workspace";
		String description = "This is a new workspace.";
		Visibility visibility = Visibility.PUBLIC;

		Member member = new Member();
		member.setId(1L);
		member.setNickname("Test User");

		Workspace workspace = new Workspace();
		workspace.setName(name);
		workspace.setDescription(description);
		workspace.setVisibility(visibility);

		Workspace savedWorkspace = new Workspace();
		savedWorkspace.setId(1L);
		savedWorkspace.setName(name);
		savedWorkspace.setDescription(description);
		savedWorkspace.setVisibility(visibility);

		when(workspaceRepository.existsByName(name)).thenReturn(false);
		when(workspaceRepository.save(workspace)).thenReturn(savedWorkspace);

		// when
		Workspace result = workspaceService.registerWorkspace(workspace, member);

		// then
		assertNotNull(result);
		assertEquals(savedWorkspace.getId(), result.getId());
		assertEquals(name, result.getName());
		assertEquals(description, result.getDescription());
		assertEquals(visibility, result.getVisibility());
		verify(workspaceRepository).existsByName(name);
		verify(workspaceRepository).save(workspace);
		verifyNoMoreInteractions(workspaceRepository);
	}

	@Test
	void testUpdateWorkspace() {
		// given
		String initialName = "Initial Workspace Name";
		String initialDescription = "Initial Workspace Description";
		Visibility initialVisibility = Visibility.PRIVATE;

		String updatedName = "Updated Workspace Name";
		String updatedDescription = "Updated Workspace Description";
		Visibility updatedVisibility = Visibility.PUBLIC;

		Member member = new Member();
		member.setId(1L);
		member.setNickname("Test User");

		Workspace workspace = new Workspace();
		workspace.setId(1L);
		workspace.setName(initialName);
		workspace.setDescription(initialDescription);
		workspace.setVisibility(initialVisibility);

		WorkspaceUpdateRequest request = new WorkspaceUpdateRequest();
		request.setName(updatedName);
		request.setDescription(updatedDescription);
		request.setVisibility(updatedVisibility);

		// Mock 생성 및 권한 검증 로직
		when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
		// 권한 부여를 위한 Mock 설정
		when(workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(workspace.getId(), member.getId()))
			.thenReturn(WorkspaceMember.builder()
				.isActive(true)
				.positionType("ADMIN")
				.build()); // 권한 부여
		when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

		// when
		workspaceService.updateWorkspace(member, workspace.getId(), Workspace.fromRequest(request));

		// then
		assertEquals(updatedName, workspace.getName());
		assertEquals(updatedDescription, workspace.getDescription());
		assertEquals(updatedVisibility, workspace.getVisibility());
		verify(workspaceRepository).findById(workspace.getId());
		verify(workspaceRepository).save(workspace);
		verify(workspaceMemberService).getWorkspaceMemberByWorkspaceIdAndMemberId(workspace.getId(), member.getId());
		// verifyNoMoreInteractions(workspaceRepository, workspaceMemberService);
	}

}
