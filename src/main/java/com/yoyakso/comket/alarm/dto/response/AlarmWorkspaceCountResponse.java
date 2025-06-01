package com.yoyakso.comket.alarm.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmWorkspaceCountResponse {
	private Long memberId;
	private Long workspaceId;
	private List<AlarmProjectCountResponse> projectAlarmList;
}
