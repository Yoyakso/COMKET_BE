package com.yoyakso.comket.ticket.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.thread.service.KafkaTopicService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketDeleteRequest;
import com.yoyakso.comket.ticket.dto.request.TicketStateUpdateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketTypeUpdateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketUpdateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.event.TicketAssignedEvent;
import com.yoyakso.comket.ticket.event.TicketStateChangedEvent;
import com.yoyakso.comket.ticket.event.TicketUpdatedEvent;
import com.yoyakso.comket.ticket.mapper.TicketMapper;
import com.yoyakso.comket.ticket.repository.TicketRepository;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
	private final TicketRepository ticketRepository;
	private final MemberService memberService;
	private final ProjectMemberService projectMemberService;
	private final ProjectService projectService;
	private final TicketMapper ticketMapper;
	private final AlarmService alarmService;
	private final KafkaTopicService kafkaTopicService;
	private final WorkspaceService workspaceService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public TicketInfoResponse createTicket(String projectName, TicketCreateRequest request,
		Member creator) {

		Ticket ticket = ticketMapper.toEntity(request, creator);

		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, creator, "티켓 생성자");
		ticket.setProject(project);

		// 부모 티켓 정보를 가져오기
		setParentTicket(ticket, request.getParentTicketId());

		// 담당자 정보 설정
		setAssignee(ticket, request.getAssigneeIdList(), project);

		// 티켓 저장
		Ticket savedTicket = ticketRepository.save(ticket);

		// 티켓 저장 후 이벤트 발행
		publishTicketAssignedEvents(savedTicket);

		kafkaTopicService.createThreadTopicIfNotExists(savedTicket.getId());

		return ticketMapper.toResponse(ticket);
	}

	// 티켓 목록 조회
	@Transactional
	public List<Ticket> getTickets(String projectName, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 조회자");

		// 삭제되지 않은 티켓 목록을 조회
		List<Ticket> ticket = ticketRepository.findByProjectAndIsDeletedFalse(project);

		//subTicketCount를 설정
		ticket.forEach(t -> {
			t.setSubTicketCount(ticketRepository.countByParentTicket(t));
		});

		return ticket;
	}

	// 티켓 상세 조회
	@Transactional
	public Ticket getTicket(String projectName, Long projectId, Long ticketId, Member member) {
		// 프로젝트 정보를 가져오기 (project_id가 우선)
		Project project = (projectId != null)
			? projectService.getProjectByProjectId(projectId, member)
			: projectService.getProjectByProjectName(projectName);

		if (project == null) {
			throw new CustomException("INVALID_PROJECT", "프로젝트 정보(project_id 또는 project_name)가 필요합니다.");
		}
		projectService.validateProjectAccess(project, member, "티켓 조회자");

		// 티켓 정보를 가져오기
		Ticket ticket = getValidTicket(ticketId);

		// 티켓이 속한 프로젝트와 요청한 프로젝트가 일치하는지 확인
		if (!ticket.getProject().equals(project)) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}
		//subTicketCount를 설정
		ticket.setSubTicketCount(ticketRepository.countByParentTicket(ticket));

		return ticket;
	}

	// 이전 버전과의 호환성을 위한 메서드
	@Transactional
	public Ticket getTicket(String projectName, Long ticketId, Member member) {
		return getTicket(projectName, null, ticketId, member);
	}

	@Transactional
	public Ticket updateTicket(String projectName, Long ticketId, TicketUpdateRequest request, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 수정자");

		// 부모 티켓 정보를 가져오기
		Ticket ticket = getTicketById(ticketId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다."));

		// 티켓이 속한 프로젝트와 요청한 프로젝트가 일치하는지 확인
		if (!ticket.getProject().equals(project)) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}

		// 변경 전 값 저장
		String oldName = ticket.getName();
		LocalDate oldStartDate = ticket.getStartDate();
		LocalDate oldEndDate = ticket.getEndDate();
		Object oldPriority = ticket.getPriority();

		// 티켓의 정보를 변경해주기
		ticketMapper.updateTicketFromRequest(ticket, request);

		// 부모 티켓 정보 설정
		setParentTicket(ticket, request.getParentTicketId());

		// 담당자 정보 설정
		setAssignee(ticket, request.getAssigneeIdList(), project);

		// 변경된 필드에 따라 이벤트 발행
		// 이름 변경 이벤트
		if (request.getName() != null && !request.getName().equals(oldName)) {
			String message = "티켓 이름이 '" + oldName + "'에서 '" + ticket.getName() + "'로 변경되었습니다.";
			eventPublisher.publishEvent(new TicketUpdatedEvent(
				ticket, member, TicketAlarmType.TICKET_NAME_CHANGED, "name", oldName, ticket.getName(), message));
		}

		// 우선순위 변경 이벤트
		if (request.getPriority() != null && !request.getPriority().equals(oldPriority)) {
			String message = "티켓 우선순위가 변경되었습니다.";
			eventPublisher.publishEvent(new TicketUpdatedEvent(
				ticket, member, TicketAlarmType.TICKET_PRIORITY_CHANGED, "priority", oldPriority, ticket.getPriority(),
				message));
		}

		// 일정 변경 이벤트 (시작일 또는 종료일 변경)
		boolean startDateChanged = (request.getStartDate() != null && !request.getStartDate().equals(oldStartDate))
			|| (oldStartDate != null && request.getStartDate() == null);
		boolean endDateChanged = (request.getEndDate() != null && !request.getEndDate().equals(oldEndDate))
			|| (oldEndDate != null && request.getEndDate() == null);

		if (startDateChanged || endDateChanged) {
			String message = "티켓 일정이 변경되었습니다.";
			eventPublisher.publishEvent(new TicketUpdatedEvent(
				ticket, member, TicketAlarmType.TICKET_DATE_CHANGED, "date",
				Map.of("startDate", oldStartDate, "endDate", oldEndDate),
				Map.of("startDate", ticket.getStartDate(), "endDate", ticket.getEndDate()),
				message));
		}

		ticketRepository.save(ticket);

		// subTicketCount를 설정
		ticket.setSubTicketCount(ticketRepository.countByParentTicket(ticket));

		// 저장하기
		return ticket;
	}

	@Transactional
	public void deleteTicket(String projectName, Long ticketId, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 삭제자");

		// 티켓 정보를 가져오기
		Ticket ticket = getTicketById(ticketId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다."));

		// 티켓이 속한 프로젝트와 요청한 프로젝트가 일치하는지 확인
		if (!ticket.getProject().equals(project)) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}
		//isDeleted를 true로 변경
		ticket.setDeleted(true);
	}

	@Transactional
	public List<Ticket> searchTickets(String projectName, Member member, List<String> requestStates,
		List<String> requestPriorities, List<Long> requestAssignees, LocalDate requestEndDate, String keyword) {
		// 프로젝트 정보 가져오기 및 접근 권한 검증
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 조회자");

		// 필터링 및 검색 로직 호출		List<Ticket> tickets = ticketRepository.findByProjectAndIsDeletedFalse(project);
		List<Ticket> tickets = ticketRepository.searchAndFilterTickets(project.getName(), requestStates,
			requestPriorities, requestAssignees, requestEndDate, keyword
		);
		//subTicketCount를 설정
		tickets.forEach(t -> {
			t.setSubTicketCount(ticketRepository.countByParentTicket(t));
		});
		return tickets;
	}

	@Transactional
	public List<Ticket> updateTicketStates(String projectName, TicketStateUpdateRequest request, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 상태 변경자");

		// 티켓 목록을 가져오기
		List<Ticket> tickets = ticketRepository.findAllById(request.getTicketIds());
		// 티켓이 비어있는지 확인
		if (tickets.isEmpty()) {
			throw new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다.");
		}
		// 티켓이 속한 프로젝트와 요청한 프로젝트가 일치하는지 확인
		if (tickets.stream().anyMatch(ticket -> !ticket.getProject().equals(project))) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}
		// 티켓 상태 변경
		tickets.forEach(ticket -> {
			ticket.setState(request.getState());

			// 티켓 상태 변경 이벤트 발행
			eventPublisher.publishEvent(new TicketStateChangedEvent(
				ticket, member, request.getState()));
		});

		ticketRepository.saveAll(tickets);
		//subTicketCount를 설정
		tickets.forEach(t -> {
			t.setSubTicketCount(ticketRepository.countByParentTicket(t));
		});
		return tickets;
	}

	@Transactional
	public List<Ticket> updateTicketTypes(String projectName, TicketTypeUpdateRequest request, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 유형 변경자");

		// 티켓 목록을 가져오기
		List<Ticket> tickets = ticketRepository.findAllById(request.getTicketIds());
		// 티켓이 비어있는지 확인
		if (tickets.isEmpty()) {
			throw new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다.");
		}
		// 티켓이 속한 프로젝트와 요청한 프로젝트가 일치하는지 확인
		if (tickets.stream().anyMatch(ticket -> !ticket.getProject().equals(project))) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}
		// 티켓 유형 변경
		tickets.forEach(ticket -> ticket.setType(request.getType()));
		ticketRepository.saveAll(tickets);
		//subTicketCount를 설정
		tickets.forEach(t -> {
			t.setSubTicketCount(ticketRepository.countByParentTicket(t));
		});
		return tickets;
	}

	@Transactional
	public void deleteTickets(String projectName, TicketDeleteRequest request, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 삭제자");

		// 티켓 목록을 가져오기
		List<Ticket> tickets = ticketRepository.findAllById(request.getTicketIds());

		if (tickets.isEmpty()) {
			throw new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다.");
		}

		if (tickets.stream().anyMatch(ticket -> !ticket.getProject().equals(project))) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}

		tickets.forEach(ticket -> ticket.setDeleted(true));
		ticketRepository.saveAll(tickets);
	}

	public Long getProjectIdByTicketId(Long ticketId) {
		return ticketRepository.findProjectIdByTicketId(ticketId);
	}

	@Transactional
	public List<Ticket> getTicketsByWorkspace(String workspaceName, Member member) {
		// 워크스페이스에 속한 프로젝트 목록을 가져오기
		Workspace workspace = workspaceService.getWorkspaceByWorkspaceName(workspaceName, member);
		// 해당 워크스페이스 내의 프로젝트에 속한 티켓 목록을 가져오기
		List<Project> projectList = projectService.getProjectsByWorkspaceAndMember(workspace, member);

		return ticketRepository.findByProjectInAndIsDeletedFalse(projectList);
	}

	// ------private------

	private Optional<Ticket> getTicketById(Long ticketId) {
		return ticketRepository.findById(ticketId);
	}

	private void setParentTicket(Ticket ticket, Long parentTicketId) {
		if (parentTicketId == null)
			return;

		Ticket parentTicket = getTicketById(parentTicketId)
			.filter(t -> !t.isDeleted())
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "부모 티켓을 찾을 수 없거나 삭제된 부모 티켓입니다."));

		ticket.setParentTicket(parentTicket);
	}

	private void setAssignee(Ticket ticket, List<Long> assigneeProjectMemberId, Project project) {
		// Check if assignees is null and initialize it if necessary
		if (ticket.getAssignees() == null) {
			ticket.setAssignees(new ArrayList<>());
		} else {
			// Clear existing assignees to avoid duplicates
			ticket.getAssignees().clear();
		}

		if (assigneeProjectMemberId == null)
			return;

		List<ProjectMember> assigneeProjectMemberList = assigneeProjectMemberId.stream()
			.map(projectMemberService::getProjectMemberByProjectMemberId)
			.toList();
		// 티켓에 담당자 설정 - add each member individually to avoid collection replacement issues
		assigneeProjectMemberList.stream()
			.map(ProjectMember::getMember)
			.forEach(member -> {
				if (!ticket.getAssignees().contains(member)) {
					ticket.getAssignees().add(member);
				}
			});
	}

	private Ticket getValidTicket(Long ticketId) {
		Ticket ticket = getTicketById(ticketId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다."));
		if (ticket.isDeleted()) {
			throw new CustomException("CANNOT_FOUND_TICKET", "삭제된 티켓입니다.");
		}
		return ticket;
	}

	private void publishTicketAssignedEvents(Ticket ticket) {
		// 티켓 담당자 지정 이벤트 발행
		ticket.getAssignees().forEach(assignee ->
			eventPublisher.publishEvent(new TicketAssignedEvent(ticket, assignee))
		);
	}

	/**
	 * 티켓 업데이트 이벤트를 발행하는 헬퍼 메서드
	 * 티켓 업데이트 이벤트를 발행하여 리스너가 알람을 처리하도록 한다
	 */
	private void sendTicketUpdateAlarm(Ticket ticket, Member updater, TicketAlarmType alarmType, String message) {
		// 필드 이름 결정
		String field;
		Object oldValue = null;
		Object newValue = null;

		switch (alarmType) {
			case TICKET_NAME_CHANGED:
				field = "name";
				break;
			case TICKET_PRIORITY_CHANGED:
				field = "priority";
				break;
			case TICKET_DATE_CHANGED:
				field = "date";
				break;
			default:
				field = "unknown";
		}

		// 티켓 업데이트 이벤트 발행
		eventPublisher.publishEvent(new TicketUpdatedEvent(
			ticket, updater, alarmType, field, oldValue, newValue, message));
	}

}
