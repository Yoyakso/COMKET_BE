package com.yoyakso.comket.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEyeLevelSummaryResponse {
	private List<String> summary;
	private LocalDateTime createTime;
}
