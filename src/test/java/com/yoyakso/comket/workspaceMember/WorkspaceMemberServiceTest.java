package com.yoyakso.comket.workspaceMember;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.yoyakso.comket.email.service.EmailService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.dto.request.WorkspaceMemberCreateRequest;
import com.yoyakso.comket.workspace.dto.response.WorkspaceMemberInfoResponse;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.WorkspaceState;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

class WorkspaceMemberServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private EmailService emailService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private WorkspaceMemberService workspaceMemberService;

    private Workspace testWorkspace;
    private Member testMember;
    private WorkspaceMember testWorkspaceMember;
    private List<WorkspaceMember> testWorkspaceMembers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");
        testWorkspace.setState(WorkspaceState.ACTIVE);

        testMember = new Member();
        testMember.setId(1L);
        testMember.setEmail("test@example.com");
        testMember.setFullName("Test User");

        testWorkspaceMember = WorkspaceMember.builder()
                .id(1L)
                .workspace(testWorkspace)
                .member(testMember)
                .nickName("Test User")
                .state(WorkspaceMemberState.ACTIVE)
                .positionType("ADMIN")
                .build();

        testWorkspaceMembers = new ArrayList<>();
        testWorkspaceMembers.add(testWorkspaceMember);
    }

    @Test
    void testCreateWorkspaceMember() {
        // Arrange
        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(testWorkspace)
                .member(testMember)
                .nickName(testMember.getFullName())
                .state(WorkspaceMemberState.ACTIVE)
                .positionType("ADMIN")
                .build();
        when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(workspaceMember);

        // Act
        workspaceMemberService.createWorkspaceMember(testWorkspace, testMember, WorkspaceMemberState.ACTIVE, "ADMIN");

        // Assert
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void testGetWorkspacesByMember() {
        // Arrange
        when(workspaceMemberRepository.findByMember(testMember)).thenReturn(testWorkspaceMembers);

        // Act
        List<Workspace> result = workspaceMemberService.getWorkspacesByMember(testMember);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspace.getId(), result.get(0).getId());
        verify(workspaceMemberRepository).findByMember(testMember);
    }

    @Test
    void testGetWorkspacesByMember_FilterInactiveWorkspace() {
        // Arrange
        testWorkspace.setState(WorkspaceState.DELETED);
        when(workspaceMemberRepository.findByMember(testMember)).thenReturn(testWorkspaceMembers);

        // Act
        List<Workspace> result = workspaceMemberService.getWorkspacesByMember(testMember);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(workspaceMemberRepository).findByMember(testMember);
    }

    @Test
    void testGetWorkspaceMemberById() {
        // Arrange
        when(workspaceMemberRepository.findById(testWorkspaceMember.getId())).thenReturn(Optional.of(testWorkspaceMember));

        // Act
        WorkspaceMember result = workspaceMemberService.getWorkspaceMemberById(testWorkspaceMember.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testWorkspaceMember.getId(), result.getId());
        verify(workspaceMemberRepository).findById(testWorkspaceMember.getId());
    }

    @Test
    void testGetWorkspaceMemberById_NotFound() {
        // Arrange
        when(workspaceMemberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            workspaceMemberService.getWorkspaceMemberById(999L);
        });
        assertEquals("CANNOT_FOUND_WORKSPACEMEMBER", exception.getCode());
        verify(workspaceMemberRepository).findById(999L);
    }

    @Test
    void testGetWorkspaceMemberByWorkspaceIdAndMemberId() {
        // Arrange
        when(workspaceMemberRepository.findByWorkspaceIdAndMemberId(testWorkspace.getId(), testMember.getId()))
                .thenReturn(Optional.of(testWorkspaceMember));

        // Act
        WorkspaceMember result = workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(
                testWorkspace.getId(), testMember.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testWorkspaceMember.getId(), result.getId());
        verify(workspaceMemberRepository).findByWorkspaceIdAndMemberId(testWorkspace.getId(), testMember.getId());
    }

    @Test
    void testGetWorkspaceMemberByWorkspaceIdAndMemberId_NotFound() {
        // Arrange
        when(workspaceMemberRepository.findByWorkspaceIdAndMemberId(999L, 999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            workspaceMemberService.getWorkspaceMemberByWorkspaceIdAndMemberId(999L, 999L);
        });
        assertEquals("CANNOT_FOUND_WORKSPACEMEMBER", exception.getCode());
        verify(workspaceMemberRepository).findByWorkspaceIdAndMemberId(999L, 999L);
    }

    @Test
    void testGetAllWorkspaceMembers() {
        // Arrange
        when(workspaceMemberRepository.findAll()).thenReturn(testWorkspaceMembers);

        // Act
        List<WorkspaceMember> result = workspaceMemberService.getAllWorkspaceMembers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspaceMember.getId(), result.get(0).getId());
        verify(workspaceMemberRepository).findAll();
    }

    @Test
    void testGetAllWorkspaceMembersByMember() {
        // Arrange
        when(workspaceMemberRepository.findByMember(testMember)).thenReturn(testWorkspaceMembers);

        // Act
        List<WorkspaceMember> result = workspaceMemberService.getAllWorkspaceMembersByMember(testMember);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspaceMember.getId(), result.get(0).getId());
        verify(workspaceMemberRepository).findByMember(testMember);
    }

    @Test
    void testUpdateWorkspaceMemberAuthority() {
        // Arrange
        WorkspaceMember updatedMember = WorkspaceMember.builder()
                .id(1L)
                .workspace(testWorkspace)
                .member(testMember)
                .state(WorkspaceMemberState.ACTIVE)
                .positionType("MEMBER") // Changed from ADMIN to MEMBER
                .build();

        when(workspaceMemberRepository.findById(testWorkspaceMember.getId())).thenReturn(Optional.of(testWorkspaceMember));
        when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(updatedMember);

        // Act
        WorkspaceMember result = workspaceMemberService.updateWorkspaceMemberAuthority(updatedMember);

        // Assert
        assertNotNull(result);
        assertEquals("MEMBER", result.getPositionType());
        verify(workspaceMemberRepository).findById(testWorkspaceMember.getId());
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(eventPublisher).publishEvent(any(com.yoyakso.comket.workspace.event.WorkspaceRoleChangedEvent.class));
    }

    @Test
    void testUpdateWorkspaceMemberInfo() {
        // Arrange
        WorkspaceMember updatedMember = WorkspaceMember.builder()
                .id(1L)
                .workspace(testWorkspace)
                .member(testMember)
                .nickName("Updated Nickname")
                .department("Engineering")
                .responsibility("Backend Developer")
                .build();

        when(workspaceMemberRepository.findById(testWorkspaceMember.getId())).thenReturn(Optional.of(testWorkspaceMember));
        when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(updatedMember);

        // Act
        WorkspaceMember result = workspaceMemberService.updateWorkspaceMemberInfo(updatedMember);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Nickname", result.getNickName());
        assertEquals("Engineering", result.getDepartment());
        assertEquals("Backend Developer", result.getResponsibility());
        verify(workspaceMemberRepository).findById(testWorkspaceMember.getId());
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
    }

    @Test
    void testSearchWorkspaceMembers() {
        // Arrange
        String keyword = "test";
        List<String> positionTypes = Arrays.asList("ADMIN", "MEMBER");
        List<String> memberStates = Arrays.asList("ACTIVE");

        when(workspaceMemberRepository.searchWorkspaceMembers(
                testWorkspace.getId(), keyword, positionTypes, memberStates))
                .thenReturn(testWorkspaceMembers);

        // Act
        List<WorkspaceMember> result = workspaceMemberService.searchWorkspaceMembers(
                testWorkspace.getId(), keyword, positionTypes, memberStates);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspaceMember.getId(), result.get(0).getId());
        verify(workspaceMemberRepository).searchWorkspaceMembers(
                testWorkspace.getId(), keyword, positionTypes, memberStates);
    }

    @Test
    void testGetWorkspaceMembersByWorkspaceId() {
        // Arrange
        when(workspaceMemberRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(testWorkspaceMembers);

        // Act
        List<WorkspaceMember> result = workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkspaceMember.getId(), result.get(0).getId());
        verify(workspaceMemberRepository).findByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testInviteMembersToWorkspace() {
        // Arrange
        WorkspaceMemberCreateRequest request = new WorkspaceMemberCreateRequest();
        request.setMemberEmailList(Arrays.asList("new@example.com"));
        request.setState(WorkspaceMemberState.ACTIVE);
        request.setPositionType("MEMBER");

        Member newMember = new Member();
        newMember.setId(2L);
        newMember.setEmail("new@example.com");
        newMember.setFullName("New User");

        WorkspaceMember newWorkspaceMember = WorkspaceMember.builder()
                .id(2L)
                .workspace(testWorkspace)
                .member(newMember)
                .nickName("New User")
                .state(WorkspaceMemberState.ACTIVE)
                .positionType("MEMBER")
                .build();

        // Create a list of existing members that doesn't include the new member's email
        List<WorkspaceMember> existingMembers = new ArrayList<>();

        // Ensure testMember has a different email than the one we're trying to invite
        testMember.setEmail("existing@example.com");
        existingMembers.add(testWorkspaceMember);

        // First call to findByWorkspaceId returns existing members
        when(workspaceMemberRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(existingMembers);

        when(memberService.getMemberByEmailOptional("new@example.com"))
                .thenReturn(Optional.of(newMember));

        when(workspaceMemberRepository.findByWorkspaceIdAndMemberId(testWorkspace.getId(), newMember.getId()))
                .thenReturn(Optional.empty());

        when(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .thenReturn(newWorkspaceMember);

        // Create updated list with both existing and new members
        List<WorkspaceMember> updatedMembers = new ArrayList<>(existingMembers);
        updatedMembers.add(newWorkspaceMember);

        // Second call to findByWorkspaceId returns updated members including the new one
        when(workspaceMemberRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(existingMembers)  // First call
                .thenReturn(updatedMembers);  // Second call

        // Mock the fileService for profile URL
        when(fileService.getFileUrlByPath(any())).thenReturn(null);

        // Create expected response
        WorkspaceMemberInfoResponse expectedResponse = WorkspaceMemberInfoResponse.builder()
                .workspaceMemberid(newWorkspaceMember.getId())
                .name(newMember.getFullName())
                .email(newMember.getEmail())
                .positionType("MEMBER")
                .state(WorkspaceMemberState.ACTIVE)
                .build();

        // Act
        List<WorkspaceMemberInfoResponse> result = workspaceMemberService.inviteMembersToWorkspace(testWorkspace, request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workspaceMemberRepository, times(2)).findByWorkspaceId(testWorkspace.getId());
        verify(memberService).getMemberByEmailOptional("new@example.com");
        verify(workspaceMemberRepository).findByWorkspaceIdAndMemberId(testWorkspace.getId(), newMember.getId());
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(eventPublisher).publishEvent(any(com.yoyakso.comket.workspace.event.WorkspaceInviteEvent.class));
    }

    @Test
    void testCountActiveWorkspaceMembers() {
        // Arrange
        when(workspaceMemberRepository.findByWorkspaceId(testWorkspace.getId())).thenReturn(testWorkspaceMembers);

        // Act
        int result = workspaceMemberService.countActiveWorkspaceMembers(testWorkspace.getId());

        // Assert
        assertEquals(1, result);
        verify(workspaceMemberRepository).findByWorkspaceId(testWorkspace.getId());
    }
}
