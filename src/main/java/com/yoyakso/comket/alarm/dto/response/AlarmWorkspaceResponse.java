package com.yoyakso.comket.alarm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.alarm.enums.WorkspaceAlarmType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmWorkspaceResponse {
	@JsonProperty("member_id")
	private Long memberId;
	
	@JsonProperty("workspace_id")
	private Long workspaceId;
	
	@JsonProperty("workspace_name")
	private String workspaceName;
	
	@JsonProperty("alarm_type")
	private WorkspaceAlarmType alarmType;
	
	@JsonProperty("alarm_message")
	private String alarmMessage;
}