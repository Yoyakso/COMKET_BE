package com.yoyakso.comket.ai.dto;

import com.yoyakso.comket.ticket.enums.TicketPriority;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionItemContentDto {
	private String title;
	private TicketPriority priority;

	@Nullable
	private ActionItemAssigneeDto memberInfo;

	@Nullable
	private String dueDate;
}
