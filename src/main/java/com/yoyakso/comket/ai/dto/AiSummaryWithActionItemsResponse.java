package com.yoyakso.comket.ai.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yoyakso.comket.ticket.enums.TicketPriority;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AiSummaryWithActionItemsResponse {
	private String summary;
	private String title;
	private TicketPriority priority;
	private ActionItemAssigneeDto memberInfo;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime sentAt;
}