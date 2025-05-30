package com.yoyakso.comket.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionItemAssigneeDto {
	private String name;
	private Long projectMemberId;
}
