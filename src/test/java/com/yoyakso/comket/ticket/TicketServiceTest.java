package com.yoyakso.comket.ticket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.enums.ProjectMemberState;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.thread.service.KafkaTopicService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketDeleteRequest;
import com.yoyakso.comket.ticket.dto.request.TicketStateUpdateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketUpdateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.enums.TicketPriority;
import com.yoyakso.comket.ticket.enums.TicketState;
import com.yoyakso.comket.ticket.enums.TicketType;
import com.yoyakso.comket.ticket.mapper.TicketMapper;
import com.yoyakso.comket.ticket.repository.TicketRepository;
import com.yoyakso.comket.ticket.service.TicketService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;

class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private ProjectService projectService;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private AlarmService alarmService;

    @Mock
    private KafkaTopicService kafkaTopicService;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TicketService ticketService;

    private Project testProject;
    private Member testMember;
    private Ticket testTicket;
    private TicketCreateRequest testCreateRequest;
    private TicketInfoResponse testTicketResponse;
    private List<ProjectMember> testAssignees;
    private Workspace testWorkspace;

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

        testAssignees = new ArrayList<>();
        ProjectMember assignee = ProjectMember.builder()
                .id(1L)
                .project(testProject)
                .member(testMember)
                .state(ProjectMemberState.ACTIVE)
                .positionType("MEMBER")
                .build();
        testAssignees.add(assignee);

        List<Member> assignees = new ArrayList<>();
        assignees.add(testMember);

        testTicket = Ticket.builder()
                .id(1L)
                .name("Test Ticket")
                .description("Test Description")
                .type(TicketType.FEATURE)
                .priority(TicketPriority.MEDIUM)
                .state(TicketState.TODO)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .project(testProject)
                .creator(testMember)
                .assignees(assignees)
                .isDeleted(false)
                .build();

        testCreateRequest = new TicketCreateRequest();
        testCreateRequest.setName("Test Ticket");
        testCreateRequest.setDescription("Test Description");
        testCreateRequest.setType(TicketType.FEATURE);
        testCreateRequest.setPriority("MEDIUM");
        testCreateRequest.setState("TODO");
        testCreateRequest.setStartDate(LocalDate.now());
        testCreateRequest.setEndDate(LocalDate.now().plusDays(7));
        testCreateRequest.setAssigneeIdList(List.of(1L));

        testTicketResponse = TicketInfoResponse.builder()
                .id(1L)
                .name("Test Ticket")
                .description("Test Description")
                .type(TicketType.FEATURE)
                .priority(TicketPriority.MEDIUM)
                .state(TicketState.TODO)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();
    }

    @Test
    void testCreateTicket() {
        // Arrange
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketMapper.toEntity(testCreateRequest, testMember)).thenReturn(testTicket);
        when(ticketRepository.save(testTicket)).thenReturn(testTicket);
        when(ticketMapper.toResponse(testTicket)).thenReturn(testTicketResponse);
        doNothing().when(kafkaTopicService).createThreadTopicIfNotExists(testTicket.getId());

        // Mock for projectMemberService.getProjectMemberByProjectMemberId
        ProjectMember projectMember = ProjectMember.builder()
                .id(1L)
                .project(testProject)
                .member(testMember)
                .state(ProjectMemberState.ACTIVE)
                .positionType("MEMBER")
                .build();
        when(projectMemberService.getProjectMemberByProjectMemberId(1L)).thenReturn(projectMember);

        // Act
        TicketInfoResponse result = ticketService.createTicket(testProject.getName(), testCreateRequest, testMember);

        // Assert
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
        assertEquals(testTicket.getName(), result.getName());
        assertEquals(testTicket.getDescription(), result.getDescription());
        assertEquals(testTicket.getType(), result.getType());
        assertEquals(testTicket.getPriority(), result.getPriority());
        assertEquals(testTicket.getState(), result.getState());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 생성자");
        verify(ticketMapper).toEntity(testCreateRequest, testMember);
        verify(ticketRepository).save(testTicket);
        verify(ticketMapper).toResponse(testTicket);
        verify(kafkaTopicService).createThreadTopicIfNotExists(testTicket.getId());
        // Use a more specific matcher for the event
        verify(eventPublisher, times(1)).publishEvent(isA(com.yoyakso.comket.ticket.event.TicketAssignedEvent.class));
    }

    @Test
    void testGetTickets() {
        // Arrange
        List<Ticket> tickets = List.of(testTicket);
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findByProjectAndIsDeletedFalse(testProject)).thenReturn(tickets);
        when(ticketRepository.countByParentTicket(testTicket)).thenReturn(0L);

        // Act
        List<Ticket> result = ticketService.getTickets(testProject.getName(), testMember);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
        assertEquals(testTicket.getName(), result.get(0).getName());
        assertEquals(0L, result.get(0).getSubTicketCount());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 조회자");
        verify(ticketRepository).findByProjectAndIsDeletedFalse(testProject);
        verify(ticketRepository).countByParentTicket(testTicket);
    }

    @Test
    void testGetTicket() {
        // Arrange
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findById(testTicket.getId())).thenReturn(Optional.of(testTicket));
        when(ticketRepository.countByParentTicket(testTicket)).thenReturn(0L);

        // Act
        Ticket result = ticketService.getTicket(testProject.getName(), testTicket.getId(), testMember);

        // Assert
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
        assertEquals(testTicket.getName(), result.getName());
        assertEquals(0L, result.getSubTicketCount());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 조회자");
        verify(ticketRepository).findById(testTicket.getId());
        verify(ticketRepository).countByParentTicket(testTicket);
    }

    @Test
    void testGetTicket_NotFound() {
        // Arrange
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            ticketService.getTicket(testProject.getName(), 999L, testMember);
        });
        assertEquals("CANNOT_FOUND_TICKET", exception.getCode());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 조회자");
        verify(ticketRepository).findById(999L);
    }

    @Test
    void testUpdateTicket() {
        // Arrange
        TicketUpdateRequest updateRequest = new TicketUpdateRequest();
        updateRequest.setName("Updated Ticket");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPriority("HIGH");
        updateRequest.setState("IN_PROGRESS");

        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findById(testTicket.getId())).thenReturn(Optional.of(testTicket));
        doNothing().when(ticketMapper).updateTicketFromRequest(testTicket, updateRequest);
        when(ticketRepository.countByParentTicket(testTicket)).thenReturn(0L);

        // Act
        Ticket result = ticketService.updateTicket(testProject.getName(), testTicket.getId(), updateRequest, testMember);

        // Assert
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
        assertEquals(0L, result.getSubTicketCount());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 수정자");
        verify(ticketRepository).findById(testTicket.getId());
        verify(ticketMapper).updateTicketFromRequest(testTicket, updateRequest);
        verify(ticketRepository).countByParentTicket(testTicket);
    }

    @Test
    void testDeleteTicket() {
        // Arrange
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Act
        ticketService.deleteTicket(testProject.getName(), testTicket.getId(), testMember);

        // Assert
        assertTrue(testTicket.isDeleted());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 삭제자");
        verify(ticketRepository).findById(testTicket.getId());
    }

    @Test
    void testSearchTickets() {
        // Arrange
        List<String> states = List.of("TODO", "IN_PROGRESS");
        List<String> priorities = List.of("HIGH", "MEDIUM");
        List<Long> assignees = List.of(1L);
        LocalDate endDate = LocalDate.now().plusDays(14);
        String keyword = "Test";

        List<Ticket> tickets = List.of(testTicket);
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.searchAndFilterTickets(
                testProject.getName(), states, priorities, assignees, endDate, keyword))
                .thenReturn(tickets);
        when(ticketRepository.countByParentTicket(testTicket)).thenReturn(0L);

        // Act
        List<Ticket> result = ticketService.searchTickets(
                testProject.getName(), testMember, states, priorities, assignees, endDate, keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
        assertEquals(0L, result.get(0).getSubTicketCount());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 조회자");
        verify(ticketRepository).searchAndFilterTickets(
                testProject.getName(), states, priorities, assignees, endDate, keyword);
        verify(ticketRepository).countByParentTicket(testTicket);
    }

    @Test
    void testUpdateTicketStates() {
        // Arrange
        TicketStateUpdateRequest request = new TicketStateUpdateRequest();
        request.setTicketIds(List.of(testTicket.getId()));
        request.setState(TicketState.DONE);

        List<Ticket> tickets = List.of(testTicket);
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findAllById(request.getTicketIds())).thenReturn(tickets);
        when(ticketRepository.saveAll(tickets)).thenReturn(tickets);
        when(ticketRepository.countByParentTicket(testTicket)).thenReturn(0L);

        // Act
        List<Ticket> result = ticketService.updateTicketStates(testProject.getName(), request, testMember);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
        assertEquals(TicketState.DONE, result.get(0).getState());
        assertEquals(0L, result.get(0).getSubTicketCount());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 상태 변경자");
        verify(ticketRepository).findAllById(request.getTicketIds());
        verify(ticketRepository).saveAll(tickets);
        verify(ticketRepository).countByParentTicket(testTicket);
        // Use a more specific matcher for the event
        verify(eventPublisher, times(1)).publishEvent(isA(com.yoyakso.comket.ticket.event.TicketStateChangedEvent.class));
    }

    @Test
    void testDeleteTickets() {
        // Arrange
        TicketDeleteRequest request = new TicketDeleteRequest();
        request.setTicketIds(List.of(testTicket.getId()));

        List<Ticket> tickets = List.of(testTicket);
        when(projectService.getProjectByProjectName(testProject.getName())).thenReturn(testProject);
        when(ticketRepository.findAllById(request.getTicketIds())).thenReturn(tickets);

        // Act
        ticketService.deleteTickets(testProject.getName(), request, testMember);

        // Assert
        assertTrue(testTicket.isDeleted());

        // Verify
        verify(projectService).getProjectByProjectName(testProject.getName());
        verify(projectService).validateProjectAccess(testProject, testMember, "티켓 삭제자");
        verify(ticketRepository).findAllById(request.getTicketIds());
        verify(ticketRepository).saveAll(tickets);
    }

    @Test
    void testGetTicketsByWorkspace() {
        // Arrange
        List<Project> projects = List.of(testProject);
        List<Ticket> tickets = List.of(testTicket);
        when(workspaceService.getWorkspaceByWorkspaceName(testWorkspace.getName(), testMember)).thenReturn(testWorkspace);
        when(projectService.getProjectsByWorkspaceAndMember(testWorkspace, testMember)).thenReturn(projects);
        when(ticketRepository.findByProjectInAndIsDeletedFalse(projects)).thenReturn(tickets);

        // Act
        List<Ticket> result = ticketService.getTicketsByWorkspace(testWorkspace.getName(), testMember);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());

        // Verify
        verify(workspaceService).getWorkspaceByWorkspaceName(testWorkspace.getName(), testMember);
        verify(projectService).getProjectsByWorkspaceAndMember(testWorkspace, testMember);
        verify(ticketRepository).findByProjectInAndIsDeletedFalse(projects);
    }
}
