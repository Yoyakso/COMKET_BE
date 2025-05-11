package com.yoyakso.comket.ticket.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
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
		Member creator) {// 요청 DTO를 엔티티로 변환

		Ticket ticket = ticketMapper.toEntity(request, projectName, creator);

		// 프로젝트 정보를 가져오기
		Project project = projectService.getProjectByProjectName(projectName);
		projectService.validateProjectAccess(project, creator, "티켓 생성자");
		ticket.setProject(project);

		// 부모 티켓 정보를 가져오기
		if (request.getParentTicketId() != null) {
			Ticket parentTicket = getTicketById(request.getParentTicketId())
				.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "부모 티켓을 찾을 수 없습니다."));
			ticket.setParentTicket(parentTicket);
		}

		// 담당자 정보 설정
		if (request.getAssigneeId() != null) {
			Member assignee = memberService.getMemberById(request.getAssigneeId());
			projectService.validateProjectAccess(project, assignee, "티켓 담당자");
			ticket.setAssignee(assignee);
		}

		// 티켓 저장
		ticketRepository.save(ticket);

		// 저장된 티켓을 응답 DTO로 변환
		return ticketMapper.toResponse(ticket);
	}

	// ------private------

	private Optional<Ticket> getTicketById(Long ticketId) {
		return ticketRepository.findById(ticketId);
	}
}
