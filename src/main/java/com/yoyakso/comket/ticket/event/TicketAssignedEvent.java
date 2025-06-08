package com.yoyakso.comket.ticket.event;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.entity.Ticket;

/**
 * Event that is published when a member is assigned to a ticket.
 */
public class TicketAssignedEvent extends TicketEvent {
    
    public TicketAssignedEvent(Ticket ticket, Member actor) {
        super(ticket, actor);
    }
}