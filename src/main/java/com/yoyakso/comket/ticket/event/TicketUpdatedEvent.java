package com.yoyakso.comket.ticket.event;

import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.entity.Ticket;

import lombok.Getter;

/**
 * Event that is published when a ticket's properties are updated.
 */
@Getter
public class TicketUpdatedEvent extends TicketEvent {
    private final TicketAlarmType alarmType;
    private final String field;
    private final Object oldValue;
    private final Object newValue;
    private final String message;
    
    public TicketUpdatedEvent(Ticket ticket, Member actor, TicketAlarmType alarmType, 
                             String field, Object oldValue, Object newValue, String message) {
        super(ticket, actor);
        this.alarmType = alarmType;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.message = message;
    }
}