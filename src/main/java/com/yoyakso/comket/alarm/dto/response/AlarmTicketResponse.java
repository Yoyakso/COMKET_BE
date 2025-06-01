package com.yoyakso.comket.alarm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.alarm.enums.TicketAlarmType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AlarmTicketResponse {
	@JsonProperty("member_id")
	private Long memberId;
	@JsonProperty("ticket_id")
	private Long ticketId;
	@JsonProperty("ticket_alarm_type")
	private TicketAlarmType ticketAlarmType;
	@JsonProperty("alarm_message")
	private String alarmMessage;
}
