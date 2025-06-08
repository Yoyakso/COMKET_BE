package com.yoyakso.comket.alarm.enums;

import java.util.List;

import lombok.Getter;

@Getter
public enum TicketAlarmType {
	// 스레드 태깅
	THREAD_TAGGING,
	// 담당자 설정
	ASSIGNEE_SETTING,
	// 티켓 담당자로 선정됨
	TICKET_ASSIGNED,
	// 티켓 상태 변경됨
	TICKET_STATE_CHANGED,
	// 티켓 우선순위 변경됨
	TICKET_PRIORITY_CHANGED,
	// 티켓 이름 변경됨
	TICKET_NAME_CHANGED,
	// 티켓 일정 변경됨
	TICKET_DATE_CHANGED;

	public static List<TicketAlarmType> getAllTypes() {
		return List.of(TicketAlarmType.values());
	}
}
