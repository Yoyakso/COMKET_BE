package com.yoyakso.comket.alarm.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.event.TicketAssignedEvent;
import com.yoyakso.comket.ticket.event.TicketStateChangedEvent;
import com.yoyakso.comket.ticket.event.TicketUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener for ticket-related events to create alarms.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketAlarmListener {

	private final AlarmService alarmService;

	/**
	 * Handle ticket assigned event
	 */
	@EventListener
	public void handleTicketAssignedEvent(TicketAssignedEvent event) {
		log.info("티켓 담당자 지정 이벤트 수신: ticketId={}, actorId={}",
			event.getTicket().getId(), event.getActor().getId());

		// 담당자에게 알람 전송 (자신 제외)
		event.getTicket().getAssignees()
			.forEach(assignee -> alarmService.addTicketAlarm(
				assignee,
				event.getTicket(),
				TicketAlarmType.TICKET_ASSIGNED,
				"티켓의 담당자로 지정되었습니다."
			));
	}

	/**
	 * Handle ticket state changed event
	 */
	@EventListener
	public void handleTicketStateChangedEvent(TicketStateChangedEvent event) {
		log.info("티켓 상태 변경 이벤트 수신: ticketId={}, actorId={}, newState={}",
			event.getTicket().getId(), event.getActor().getId(), event.getNewState());

		Ticket ticket = event.getTicket();
		Member actor = event.getActor();
		String message = "티켓 상태가 " + event.getNewStateAsString() + "로 변경되었습니다.";

		// 생성자에게 알람 전송 (자신 제외)
		if (!ticket.getCreator().equals(actor)) {
			alarmService.addTicketAlarm(ticket.getCreator(), ticket, TicketAlarmType.TICKET_STATE_CHANGED, message);
		}

		// 담당자들에게 알람 전송 (자신 제외)
		ticket.getAssignees().stream()
			.filter(assignee -> !assignee.equals(actor))
			.forEach(assignee -> alarmService.addTicketAlarm(
				assignee, ticket, TicketAlarmType.TICKET_STATE_CHANGED, message
			));
	}

	/**
	 * Handle ticket updated event
	 */
	@EventListener
	public void handleTicketUpdatedEvent(TicketUpdatedEvent event) {
		log.info("티켓 업데이트 이벤트 수신: ticketId={}, actorId={}, field={}",
			event.getTicket().getId(), event.getActor().getId(), event.getField());

		Ticket ticket = event.getTicket();
		Member actor = event.getActor();

		// 생성자에게 알람 전송 (자신 제외)
		if (!ticket.getCreator().equals(actor)) {
			alarmService.addTicketAlarm(ticket.getCreator(), ticket, event.getAlarmType(), event.getMessage());
		}

		// 담당자들에게 알람 전송 (자신 제외)
		ticket.getAssignees().stream()
			.filter(assignee -> !assignee.equals(actor))
			.forEach(assignee -> alarmService.addTicketAlarm(
				assignee, ticket, event.getAlarmType(), event.getMessage()
			));
	}
}
