package com.yoyakso.comket.ticket.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.yoyakso.comket.ticket.enums.TicketPriority;
import com.yoyakso.comket.ticket.enums.TicketState;
import com.yoyakso.comket.ticket.enums.TicketType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketInfoResponse {
	private Long id;
	private String name;
	private String description;
	private TicketType type;
	private Long parentTicketId;
	private TicketPriority priority;
	private TicketState state;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Long assigneeId;
	private Long creatorId;
}