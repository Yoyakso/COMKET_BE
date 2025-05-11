package com.yoyakso.comket.ticket.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketUpdateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.mapper.TicketMapper;
import com.yoyakso.comket.ticket.repository.TicketRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
	private final TicketRepository ticketRepository;
	private final MemberService memberService;
	private final ProjectService projectService;
	private final TicketMapper ticketMapper;

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
		setAssignee(ticket, request.getAssigneeId(), project);

		// 티켓 저장
		ticketRepository.save(ticket);

		return ticketMapper.toResponse(ticket);
	}

	// 티켓 목록 조회
	@Transactional
	public List<Ticket> getTickets(String projectName, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 조회자");
		// 삭제되지 않은 티켓 목록을 조회
		return ticketRepository.findByProjectAndIsDeletedFalse(project);
	}

	// 티켓 상세 조회
	@Transactional
	public Ticket getTicket(String projectName, Long ticketId, Member member) {
		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 조회자");

		// 티켓 정보를 가져오기
		Ticket ticket = getValidTicket(ticketId);

		// 티켓이 속한 프로젝트와 요청한 프로젝트가 일치하는지 확인
		if (!ticket.getProject().equals(project)) {
			throw new CustomException("INVALID_PROJECT", "요청한 프로젝트와 티켓의 프로젝트가 일치하지 않습니다.");
		}

		return ticket;
	}

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

		// 티켓의 정보를 변경해주기
		ticketMapper.updateTicketFromRequest(ticket, request);

		// 부모 티켓 정보 설정
		setParentTicket(ticket, request.getParentTicketId());
		// 담당자 정보 설정
		setAssignee(ticket, request.getAssigneeId(), project);

		// 저장하기
		return ticketRepository.save(ticket);
	}

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

	// ------private------

	private Optional<Ticket> getTicketById(Long ticketId) {
		return ticketRepository.findById(ticketId);
	}

	private void setParentTicket(Ticket ticket, Long parentTicketId) {
		if (parentTicketId != null) {
			Ticket parentTicket = getTicketById(parentTicketId)
				.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "부모 티켓을 찾을 수 없습니다."));
			ticket.setParentTicket(parentTicket);
		}
	}

	private void setAssignee(Ticket ticket, Long assigneeId, Project project) {
		if (assigneeId != null) {
			Member assignee = memberService.getMemberById(assigneeId);
			projectService.validateProjectAccess(project, assignee, "티켓 담당자");
			ticket.setAssignee(assignee);
		}
	}

	private Ticket getValidTicket(Long ticketId) {
		Ticket ticket = getTicketById(ticketId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다."));
		if (ticket.isDeleted()) {
			throw new CustomException("CANNOT_FOUND_TICKET", "삭제된 티켓입니다.");
		}
		return ticket;
	}

	public List<Ticket> searchTickets(String projectName, String query, Member member) {
		// 프로젝트 정보를 가져오기ß
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, member, "티켓 검색자");

		// 삭제되지 않은 티켓 목록을 조회
		return ticketRepository.findByProjectAndIsDeletedFalseAndNameContaining(project, query);
	}
}
