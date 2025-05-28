package com.yoyakso.comket.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AiSummaryWithActionItemsResponse {
	private String summary;
	private ActionItemContentDto[] actionItems;
}