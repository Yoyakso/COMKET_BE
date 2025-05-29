package com.yoyakso.comket.ai.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiSummaryWithActionItemsResponse {
	private String summary;
	private ActionItemContentDto[] actionItems;
	private LocalDateTime createTime;
}