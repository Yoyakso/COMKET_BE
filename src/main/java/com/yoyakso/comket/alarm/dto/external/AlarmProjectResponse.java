package com.yoyakso.comket.alarm.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmProjectResponse {
	private Long memberId;
	private Long projectId;
	private String projectName;
	private Long alarmCount;
}
