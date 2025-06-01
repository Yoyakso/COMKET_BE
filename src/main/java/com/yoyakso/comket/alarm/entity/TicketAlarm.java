package com.yoyakso.comket.alarm.entity;

import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.entity.Ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketAlarm {
	// 티켓 ID
	private Ticket ticket;

	// 멤버 ID
	private Member member;

	// 티켓 관련 알림 종류
	private TicketAlarmType alarmType;

	// 알람 메세지
	private String alarmMessage = "";
}
