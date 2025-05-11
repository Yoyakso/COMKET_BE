package com.yoyakso.comket.ticket.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;

@Component
public class TicketMapper {
	@Autowired
	private MemberService memberService;

	public Ticket toEntity(TicketCreateRequest request, Member creator) {
		return Ticket.builder()
			.name(request.getName())
			.description(request.getDescription())
			.type(request.getType())
			.priority(request.getPriority())
			.state(request.getState())
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.creator(creator)
			.build();
	}

	public TicketInfoResponse toResponse(Ticket ticket) {
		return TicketInfoResponse.builder()
			.id(ticket.getId())
			.name(ticket.getName())
			.description(ticket.getDescription())
			.type(ticket.getType())
			.priority(ticket.getPriority())
			.state(ticket.getState())
			.startDate(ticket.getStartDate())
			.endDate(ticket.getEndDate())
			.createdAt(ticket.getCreatedAt())
			.updatedAt(ticket.getUpdatedAt())
			.assigneeMember(
				ticket.getAssignee() != null ? memberService.buildMemberInfoResponse(ticket.getAssignee()) : null)
			.creatorMember(memberService.buildMemberInfoResponse(ticket.getCreator()))
			.parentTicketId(ticket.getParentTicket() != null ? ticket.getParentTicket().getId() : null)
			.build();
	}
}