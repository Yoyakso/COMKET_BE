package com.yoyakso.comket.alarm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmProjectCountResponse {
	@JsonProperty("member_id")
	private Long memberId;
	@JsonProperty("project_id")
	private Long projectId;
	@JsonProperty("project_name")
	private String projectName;
	@JsonProperty("alarm_count")
	private Long alarmCount;
}
