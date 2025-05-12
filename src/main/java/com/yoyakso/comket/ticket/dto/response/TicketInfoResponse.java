package com.yoyakso.comket.ticket.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.member.dto.MemberInfoResponse;
import com.yoyakso.comket.ticket.enums.TicketPriority;
import com.yoyakso.comket.ticket.enums.TicketState;
import com.yoyakso.comket.ticket.enums.TicketType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketInfoResponse {
	private Long id;
	@JsonProperty("ticket_name")
	private String name;
	private String description;
	@JsonProperty("ticket_type")
	private TicketType type;
	@JsonProperty("parent_ticket_id")
	private Long parentTicketId;
	@JsonProperty("ticket_priority")
	private TicketPriority priority;
	@JsonProperty("ticket_state")
	private TicketState state;
	@JsonProperty("start_date")
	private LocalDate startDate;
	@JsonProperty("end_date")
	private LocalDate endDate;
	@JsonProperty("created_at")
	private LocalDateTime createdAt;
	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;
	@JsonProperty("assignee_member")
	private MemberInfoResponse assigneeMember;
	@JsonProperty("creator_member")
	private MemberInfoResponse creatorMember;
	@JsonProperty("sub_ticket_count")
	private Long subTicketCount;
}