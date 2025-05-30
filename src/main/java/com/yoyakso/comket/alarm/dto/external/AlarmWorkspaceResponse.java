package com.yoyakso.comket.alarm.dto.external;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmWorkspaceResponse {
	private Long memberId;
	private Long workspaceId;
	private List<AlarmProjectResponse> projectAlarmList;
}
