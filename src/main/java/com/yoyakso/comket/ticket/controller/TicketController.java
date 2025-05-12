package com.yoyakso.comket.ticket.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketDeleteRequest;
import com.yoyakso.comket.ticket.dto.request.TicketStateUpdateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketTypeUpdateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketUpdateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.mapper.TicketMapper;
import com.yoyakso.comket.ticket.service.TicketService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {
	private final TicketService ticketService;
	private final TicketMapper ticketMapper;
	private final MemberService memberService;

	// 티켓 생성
	@PostMapping("")
	public ResponseEntity<TicketInfoResponse> createTicket(
		@RequestParam("project_name") String projectName,
		@Valid @RequestBody TicketCreateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		TicketInfoResponse info = ticketService.createTicket(projectName, request, member);
		return ResponseEntity.ok(info);
	}

	// 티켓 목록 조회
	@GetMapping("")
	public ResponseEntity<List<TicketInfoResponse>> getTickets(
		@RequestParam("project_name") String projectName
	) {
		Member member = memberService.getAuthenticatedMember();
		List<Ticket> tickets = ticketService.getTickets(projectName, member);
		return ResponseEntity.ok(tickets.stream()
			.map(ticketMapper::toResponse)
			.toList());
	}

	// 티켓 상세 조회
	@GetMapping("/{ticket_id}")
	public ResponseEntity<TicketInfoResponse> getTicket(
		@RequestParam("project_name") String projectName,
		@PathVariable("ticket_id") Long ticketId
	) {
		Member member = memberService.getAuthenticatedMember();
		Ticket ticket = ticketService.getTicket(projectName, ticketId, member);
		return ResponseEntity.ok(ticketMapper.toResponse(ticket));
	}

	// 티켓 수정
	@PatchMapping("/{ticket_id}")
	public ResponseEntity<TicketInfoResponse> updateTicket(
		@RequestParam("project_name") String projectName,
		@PathVariable("ticket_id") Long ticketId,
		@Valid @RequestBody TicketUpdateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		Ticket ticket = ticketService.updateTicket(projectName, ticketId, request, member);
		return ResponseEntity.ok(ticketMapper.toResponse(ticket));
	}

	// 여러 티켓 상태 변경
	@PatchMapping("/state")
	public ResponseEntity<List<TicketInfoResponse>> updateTicketStates(
		@RequestParam("project_name") String projectName,
		@Valid @RequestBody TicketStateUpdateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		List<Ticket> updatedTickets = ticketService.updateTicketStates(projectName, request, member);
		return ResponseEntity.ok(updatedTickets.stream()
			.map(ticketMapper::toResponse)
			.toList());
	}

	// 여러 티켓 유형 변경
	@PatchMapping("/type")
	public ResponseEntity<List<TicketInfoResponse>> updateTicketTypes(
		@RequestParam("project_name") String projectName,
		@Valid @RequestBody TicketTypeUpdateRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		List<Ticket> updatedTickets = ticketService.updateTicketTypes(projectName, request, member);
		return ResponseEntity.ok(updatedTickets.stream()
			.map(ticketMapper::toResponse)
			.toList());
	}

	// 여러 티켓 삭제
	@DeleteMapping("")
	public ResponseEntity<Void> deleteTickets(
		@RequestParam("project_name") String projectName,
		@Valid @RequestBody TicketDeleteRequest request
	) {
		Member member = memberService.getAuthenticatedMember();
		ticketService.deleteTickets(projectName, request, member);
		return ResponseEntity.noContent().build();
	}

	// 개별 티켓 삭제
	@DeleteMapping("/{ticket_id}")
	public ResponseEntity<Void> deleteTicket(
		@RequestParam("project_name") String projectName,
		@PathVariable("ticket_id") Long ticketId
	) {
		Member member = memberService.getAuthenticatedMember();
		ticketService.deleteTicket(projectName, ticketId, member);
		return ResponseEntity.noContent().build();
	}

	// 티켓 검색
	@GetMapping("/search")
	public ResponseEntity<List<TicketInfoResponse>> searchTickets(
		@RequestParam("project_name") String projectName,
		// 티켓 상태 필터링
		@RequestParam(value = "state", required = false) List<String> requestStates,
		// 티켓 우선순위 필터링
		@RequestParam(value = "priority", required = false) List<String> requestPriorities,
		// 티켓 담당자 필터링
		@RequestParam(value = "assignee_member_id", required = false) List<Long> requestAssignees,
		// 마감 일자 필터링
		@RequestParam(value = "end_date", required = false) LocalDate requestEndDate,
		// 키워드 검색
		@RequestParam(value = "keyword", required = false) String keyword
	) {
		Member member = memberService.getAuthenticatedMember();
		List<Ticket> tickets = ticketService.searchTickets(projectName, member,
			requestStates, requestPriorities, requestAssignees, requestEndDate, keyword);
		return ResponseEntity.ok(tickets.stream()
			.map(ticketMapper::toResponse)
			.toList());
	}
}
