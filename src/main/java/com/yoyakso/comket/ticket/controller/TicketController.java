package com.yoyakso.comket.ticket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
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

}
