package com.yoyakso.comket.ticket.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.repository.TicketRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternalTicketService {
	private final TicketRepository ticketRepository;
	private final ProjectService projectService;

	@Transactional
	public Ticket getTicketByIdAndMember(Long ticketId, Member member) {
		return getValidTicket(ticketId);
	}

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

	private Ticket getValidTicket(Long ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_TICKET", "티켓을 찾을 수 없습니다."));
		if (ticket.isDeleted()) {
			throw new CustomException("CANNOT_FOUND_TICKET", "삭제된 티켓입니다.");
		}
		return ticket;
	}
}
