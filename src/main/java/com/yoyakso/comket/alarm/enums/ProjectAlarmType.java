package com.yoyakso.comket.alarm.enums;

import java.util.List;

import lombok.Getter;

@Getter
public enum ProjectAlarmType {
	// 프로젝트 멤버 추가
	PROJECT_INVITE;

	public static List<ProjectAlarmType> getAllTypes() {
		return List.of(ProjectAlarmType.values());
	}
}