package com.yoyakso.comket.projectMember;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.dto.ProjectMemberInviteRequest;
import com.yoyakso.comket.project.dto.ProjectMemberResponse;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.enums.ProjectMemberState;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

class ProjectMemberServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private WorkspaceMemberService workspaceMemberService;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private Project testProject;
    private Member testMember;
    private Workspace testWorkspace;
    private ProjectMember testProjectMember;
    private WorkspaceMember testWorkspaceMember;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");

        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .workspace(testWorkspace)
                .build();

        testMember = new Member();
        testMember.setId(1L);
        testMember.setEmail("test@example.com");
        testMember.setFullName("Test User");

        testProjectMember = ProjectMember.builder()
                .id(1L)
                .project(testProject)
                .member(testMember)
                .state(ProjectMemberState.ACTIVE)
                .positionType("ADMIN")
                .build();

        testWorkspaceMember = WorkspaceMember.builder()
                .id(1L)
                .workspace(testWorkspace)
                .member(testMember)
                .state(WorkspaceMemberState.ACTIVE)
                .positionType("ADMIN")
                .build();
    }

    @Test
    void testAddProjectMember() {
        // Arrange
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(testProjectMember);

        // Act
        ProjectMember result = projectMemberService.addProjectMember(testProject, testMember, "ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals(testProjectMember.getId(), result.getId());
        assertEquals(testProjectMember.getProject().getId(), result.getProject().getId());
        assertEquals(testProjectMember.getMember().getId(), result.getMember().getId());
        assertEquals(testProjectMember.getState(), result.getState());
        assertEquals(testProjectMember.getPositionType(), result.getPositionType());
        verify(projectMemberRepository).save(any(ProjectMember.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void testGetProjectMemberByProjectMemberId() {
        // Arrange
        when(projectMemberRepository.findById(testProjectMember.getId())).thenReturn(Optional.of(testProjectMember));

        // Act
        ProjectMember result = projectMemberService.getProjectMemberByProjectMemberId(testProjectMember.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testProjectMember.getId(), result.getId());
        verify(projectMemberRepository).findById(testProjectMember.getId());
    }

    @Test
    void testGetProjectMemberByProjectMemberId_NotFound() {
        // Arrange
        when(projectMemberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.getProjectMemberByProjectMemberId(999L);
        });
        assertEquals("CANNOT_FOUND_PROJECTMEMBER", exception.getCode());
        verify(projectMemberRepository).findById(999L);
    }

    @Test
    void testGetProjectMemberByProjectIdAndMemberId() {
        // Arrange
        when(projectMemberRepository.findByProjectIdAndMemberIdAndState(
                testProject.getId(), testMember.getId(), ProjectMemberState.ACTIVE))
                .thenReturn(Optional.of(testProjectMember));

        // Act
        ProjectMember result = projectMemberService.getProjectMemberByProjectIdAndMemberId(
                testProject.getId(), testMember.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testProjectMember.getId(), result.getId());
        verify(projectMemberRepository).findByProjectIdAndMemberIdAndState(
                testProject.getId(), testMember.getId(), ProjectMemberState.ACTIVE);
    }

    @Test
    void testGetProjectMemberByProjectIdAndMemberId_NotFound() {
        // Arrange
        when(projectMemberRepository.findByProjectIdAndMemberIdAndState(
                999L, 999L, ProjectMemberState.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.getProjectMemberByProjectIdAndMemberId(999L, 999L);
        });
        assertEquals("CANNOT_FOUND_PROJECTMEMBER", exception.getCode());
        verify(projectMemberRepository).findByProjectIdAndMemberIdAndState(
                999L, 999L, ProjectMemberState.ACTIVE);
    }

    @Test
    void testGetProjectListByMemberAndWorkspace() {
        // Arrange
        List<Project> projects = new ArrayList<>();
        projects.add(testProject);
        when(projectMemberRepository.findAllProjectsByMemberAndWorkspace(testMember, testWorkspace))
                .thenReturn(projects);

        // Act
        List<Project> result = projectMemberService.getProjectListByMemberAndWorkspace(testMember, testWorkspace);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getId(), result.get(0).getId());
        verify(projectMemberRepository).findAllProjectsByMemberAndWorkspace(testMember, testWorkspace);
    }

    @Test
    void testGetMemberIdByProjectMemberId() {
        // Arrange
        when(projectMemberRepository.findMemberIdById(testProjectMember.getId()))
                .thenReturn(Optional.of(testMember.getId()));

        // Act
        Long result = projectMemberService.getMemberIdByProjectMemberId(testProjectMember.getId());

        // Assert
        assertEquals(testMember.getId(), result);
        verify(projectMemberRepository).findMemberIdById(testProjectMember.getId());
    }

    @Test
    void testGetMemberIdByProjectMemberId_NotFound() {
        // Arrange
        when(projectMemberRepository.findMemberIdById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.getMemberIdByProjectMemberId(999L);
        });
        assertEquals("CANNOT_FOUND_MEMBER_ID", exception.getCode());
        verify(projectMemberRepository).findMemberIdById(999L);
    }

    @Test
    void testInviteMembersToProject_EmptyList() {
        // Arrange
        ProjectMemberInviteRequest request = ProjectMemberInviteRequest.builder()
                .workspaceMemberIdList(new ArrayList<>())
                .build();

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.inviteMembersToProject(testProject.getId(), request);
        });
        assertEquals("INVALID_MEMBER_LIST", exception.getCode());
    }

    @Test
    void testInviteMembersToProject_Success() {
        // Arrange
        List<Long> workspaceMemberIds = new ArrayList<>();
        workspaceMemberIds.add(testWorkspaceMember.getId());
        ProjectMemberInviteRequest request = ProjectMemberInviteRequest.builder()
                .workspaceMemberIdList(workspaceMemberIds)
                .positionType("MEMBER")
                .build();

        List<ProjectMember> existingMembers = new ArrayList<>();

        when(projectMemberRepository.findAllByProjectId(testProject.getId())).thenReturn(existingMembers);
        when(workspaceMemberService.getWorkspaceMemberById(testWorkspaceMember.getId())).thenReturn(testWorkspaceMember);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(memberService.getMemberById(testMember.getId())).thenReturn(testMember);

        ProjectMember newProjectMember = ProjectMember.builder()
                .id(2L)
                .project(testProject)
                .member(testMember)
                .state(ProjectMemberState.ACTIVE)
                .positionType("MEMBER")
                .build();

        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(newProjectMember);

        // Act
        List<ProjectMemberResponse> result = projectMemberService.inviteMembersToProject(
                testProject.getId(), request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(newProjectMember.getId(), result.get(0).getProjectMemberId());
        assertEquals(testMember.getFullName(), result.get(0).getName());
        assertEquals(testMember.getEmail(), result.get(0).getEmail());
        assertEquals(newProjectMember.getPositionType(), result.get(0).getPositionType());
        assertEquals(newProjectMember.getState(), result.get(0).getState());

        verify(projectMemberRepository).findAllByProjectId(testProject.getId());
        verify(workspaceMemberService).getWorkspaceMemberById(testWorkspaceMember.getId());
        verify(projectRepository).findById(testProject.getId());
        verify(memberService).getMemberById(testMember.getId());
        verify(projectMemberRepository).save(any(ProjectMember.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void testFindActiveProjectMemberByEmail() {
        // Arrange
        when(projectMemberRepository.findActiveProjectMemberByProjectIdAndMemberEmail(
                testProject.getId(), testMember.getEmail(), ProjectMemberState.ACTIVE))
                .thenReturn(Optional.of(testProjectMember));

        // Act
        ProjectMember result = projectMemberService.findActiveProjectMemberByEmail(
                testProject.getId(), testMember.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testProjectMember.getId(), result.getId());
        verify(projectMemberRepository).findActiveProjectMemberByProjectIdAndMemberEmail(
                testProject.getId(), testMember.getEmail(), ProjectMemberState.ACTIVE);
    }

    @Test
    void testFindActiveProjectMemberByEmail_NotFound() {
        // Arrange
        when(projectMemberRepository.findActiveProjectMemberByProjectIdAndMemberEmail(
                testProject.getId(), "nonexistent@example.com", ProjectMemberState.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.findActiveProjectMemberByEmail(
                    testProject.getId(), "nonexistent@example.com");
        });
        assertEquals("CANNOT_FOUND_PROJECTMEMBER", exception.getCode());
        verify(projectMemberRepository).findActiveProjectMemberByProjectIdAndMemberEmail(
                testProject.getId(), "nonexistent@example.com", ProjectMemberState.ACTIVE);
    }

    @Test
    void testFindProjectMemberIdByProjectIdAndMemberId() {
        // Arrange
        when(projectMemberRepository.findIdByProjectIdAndMemberId(testProject.getId(), testMember.getId()))
                .thenReturn(Optional.of(testProjectMember.getId()));

        // Act
        Long result = projectMemberService.findProjectMemberIdByProjectIdAndMemberId(
                testProject.getId(), testMember.getId());

        // Assert
        assertEquals(testProjectMember.getId(), result);
        verify(projectMemberRepository).findIdByProjectIdAndMemberId(testProject.getId(), testMember.getId());
    }

    @Test
    void testFindProjectMemberIdByProjectIdAndMemberId_NotFound() {
        // Arrange
        when(projectMemberRepository.findIdByProjectIdAndMemberId(999L, 999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.findProjectMemberIdByProjectIdAndMemberId(999L, 999L);
        });
        assertEquals("CANNOT_FOUND_PROJECTMEMBER", exception.getCode());
        verify(projectMemberRepository).findIdByProjectIdAndMemberId(999L, 999L);
    }

    @Test
    void testFindProjectMemberIdByWorkspaceMemberIdAndProjectId() {
        // Arrange
        when(projectMemberRepository.findActiveProjectMemberIdByWorkspaceMemberIdAndProjectId(
                testWorkspaceMember.getId(), testProject.getId(), ProjectMemberState.ACTIVE))
                .thenReturn(Optional.of(testProjectMember.getId()));

        // Act
        Long result = projectMemberService.findProjectMemberIdByWorkspaceMemberIdAndProjectId(
                testWorkspaceMember.getId(), testProject.getId());

        // Assert
        assertEquals(testProjectMember.getId(), result);
        verify(projectMemberRepository).findActiveProjectMemberIdByWorkspaceMemberIdAndProjectId(
                testWorkspaceMember.getId(), testProject.getId(), ProjectMemberState.ACTIVE);
    }

    @Test
    void testFindProjectMemberIdByWorkspaceMemberIdAndProjectId_NotFound() {
        // Arrange
        when(projectMemberRepository.findActiveProjectMemberIdByWorkspaceMemberIdAndProjectId(
                999L, 999L, ProjectMemberState.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            projectMemberService.findProjectMemberIdByWorkspaceMemberIdAndProjectId(999L, 999L);
        });
        assertEquals("CANNOT_FOUND_PROJECTMEMBER", exception.getCode());
        verify(projectMemberRepository).findActiveProjectMemberIdByWorkspaceMemberIdAndProjectId(
                999L, 999L, ProjectMemberState.ACTIVE);
    }

    @Test
    void testBuildProjectMemberInfoResponse() {
        // Arrange
        when(projectMemberRepository.findByProjectIdAndMemberIdAndState(
                testProject.getId(), testMember.getId(), ProjectMemberState.ACTIVE))
                .thenReturn(Optional.of(testProjectMember));

        // Act
        ProjectMemberResponse result = projectMemberService.buildProjectMemberInfoResponse(testProject, testMember);

        // Assert
        assertNotNull(result);
        assertEquals(testProjectMember.getId(), result.getProjectMemberId());
        assertEquals(testMember.getFullName(), result.getName());
        assertEquals(testMember.getEmail(), result.getEmail());
        assertEquals(testProjectMember.getPositionType(), result.getPositionType());
        assertEquals(testProjectMember.getState(), result.getState());
        verify(projectMemberRepository).findByProjectIdAndMemberIdAndState(
                testProject.getId(), testMember.getId(), ProjectMemberState.ACTIVE);
    }

    @Test
    void testBuildProjectMemberInfoListResponse() {
        // Arrange
        List<Member> members = new ArrayList<>();
        members.add(testMember);

        when(projectMemberRepository.findByProjectIdAndMemberIdAndState(
                testProject.getId(), testMember.getId(), ProjectMemberState.ACTIVE))
                .thenReturn(Optional.of(testProjectMember));

        // Act
        List<ProjectMemberResponse> result = projectMemberService.buildProjectMemberInfoListResponse(testProject, members);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProjectMember.getId(), result.get(0).getProjectMemberId());
        assertEquals(testMember.getFullName(), result.get(0).getName());
        assertEquals(testMember.getEmail(), result.get(0).getEmail());
        assertEquals(testProjectMember.getPositionType(), result.get(0).getPositionType());
        assertEquals(testProjectMember.getState(), result.get(0).getState());
        verify(projectMemberRepository).findByProjectIdAndMemberIdAndState(
                testProject.getId(), testMember.getId(), ProjectMemberState.ACTIVE);
    }
}
