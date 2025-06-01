package com.yoyakso.comket.alarm.enums;

import java.util.List;

import lombok.Getter;

@Getter
public enum TicketAlarmType {
	// 스레드 태깅
	THREAD_TAGGING,
	// 담당자 설정
	ASSIGNEE_SETTING;

	public static List<TicketAlarmType> getAllTypes() {
		return List.of(TicketAlarmType.values());
	}
}