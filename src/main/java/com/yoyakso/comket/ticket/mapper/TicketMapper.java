package com.yoyakso.comket.ticket.mapper;

import org.springframework.stereotype.Component;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;

@Component
public class TicketMapper {
	public Ticket toEntity(TicketCreateRequest request, String projectName, Member creator) {
		return Ticket.builder()
			.name(request.getName())
			.description(request.getDescription())
			.type(request.getType())
			.priority(request.getPriority())
			.state(request.getState())
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.creator(creator)
			// .project(new Project(projectName))
			// .parentTicket(
			// 	request.getParentTicketId() != null ? Ticket.builder().id(request.getParentTicketId()).build() : null)
			// .assignee(request.getAssigneeId() != null ? Member.builder().id(request.getAssigneeId()).build() : null)
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
			.assigneeId(ticket.getAssignee() != null ? ticket.getAssignee().getId() : null)
			.creatorId(ticket.getCreator().getId())
			.parentTicketId(ticket.getParentTicket() != null ? ticket.getParentTicket().getId() : null)
			.build();
	}
}