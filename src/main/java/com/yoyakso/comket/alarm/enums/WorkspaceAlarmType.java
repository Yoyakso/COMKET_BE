package com.yoyakso.comket.alarm.enums;

import java.util.List;

import lombok.Getter;

@Getter
public enum WorkspaceAlarmType {
	// 워크스페이스 초대
	WORKSPACE_INVITE,
	// 워크스페이스 역할 변경
	WORKSPACE_POSITIONTYPE_CHANGED;

	public static List<WorkspaceAlarmType> getAllTypes() {
		return List.of(WorkspaceAlarmType.values());
	}
}