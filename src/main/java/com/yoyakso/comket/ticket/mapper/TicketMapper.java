package com.yoyakso.comket.ticket.mapper;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.ticket.dto.request.TicketCreateRequest;
import com.yoyakso.comket.ticket.dto.request.TicketUpdateRequest;
import com.yoyakso.comket.ticket.dto.response.TicketInfoResponse;
import com.yoyakso.comket.ticket.dto.response.TicketProjectInfoResponse;
import com.yoyakso.comket.ticket.entity.Ticket;

@Component
public class TicketMapper {
	@Autowired
	private ProjectMemberService projectMemberService;

	public Ticket toEntity(TicketCreateRequest request, Member creator) {
		return Ticket.builder()
			.name(request.getName())
			.description(request.getDescription())
			.type(request.getType())
			.priority(request.getPriority())
			.state(request.getState())
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.additionalInfo(request.getAdditionalInfo()) // 추가 정보 매핑
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
			.assigneeMemberList(
				ticket.getAssignees() != null && !ticket.getAssignees().isEmpty() ?
					projectMemberService.buildProjectMemberInfoListResponse(ticket.getProject(),
						ticket.getAssignees()) : null
			)
			.creatorMember(
				projectMemberService.buildProjectMemberInfoResponse(ticket.getProject(), ticket.getCreator()))
			.parentTicketId(ticket.getParentTicket() != null ? ticket.getParentTicket().getId() : null)
			.subTicketCount(ticket.getSubTicketCount())
			.additionalInfo(ticket.getAdditionalInfo()) // 추가 정보 매핑
			.build();
	}

	private <T> void updateField(Consumer<T> setter, T value, boolean isNullable, String errorCode,
		String errorMessage) {
		if (value != null) {
			setter.accept(value);
		} else if (!isNullable) {
			throw new CustomException(errorCode, errorMessage);
		} else {
			setter.accept(null);
		}
	}

	public void updateTicketFromRequest(Ticket ticket, TicketUpdateRequest request) {
		updateField(ticket::setName, request.getName(), false, "INVALID_TICKET_NAME", "티켓 이름은 null이 될 수 없습니다.");
		updateField(ticket::setDescription, request.getDescription(), true, null, null);
		updateField(ticket::setType, request.getType(), true, null, null);
		updateField(ticket::setPriority, request.getPriority(), true, null, null);
		updateField(ticket::setState, request.getState(), false, "INVALID_TICKET_STATE", "티켓 상태는 null이 될 수 없습니다.");
		updateField(ticket::setStartDate, request.getStartDate(), true, null, null);
		updateField(ticket::setEndDate, request.getEndDate(), true, null, null);
		updateField(ticket::setAdditionalInfo, request.getAdditionalInfo(), true, null, null); // 추가 정보 처리
	}

	public TicketProjectInfoResponse toWorkspaceResponse(Ticket ticket) {
		return TicketProjectInfoResponse.builder()
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
			.assigneeMemberList(
				ticket.getAssignees() != null && !ticket.getAssignees().isEmpty() ?
					projectMemberService.buildProjectMemberInfoListResponse(ticket.getProject(),
						ticket.getAssignees()) : null
			)
			.creatorMember(
				projectMemberService.buildProjectMemberInfoResponse(ticket.getProject(), ticket.getCreator()))
			.parentTicketId(ticket.getParentTicket() != null ? ticket.getParentTicket().getId() : null)
			.subTicketCount(ticket.getSubTicketCount())
			.additionalInfo(ticket.getAdditionalInfo()) // 추가 정보 매핑
			.build();
	}
}