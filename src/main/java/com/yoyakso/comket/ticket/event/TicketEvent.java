package com.yoyakso.comket.ticket.event;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.entity.Ticket;

import lombok.Getter;

/**
 * Base event class for ticket-related events.
 * Contains common properties for all ticket events.
 */
@Getter
public abstract class TicketEvent {
    private final Ticket ticket;
    private final Member actor;
    
    public TicketEvent(Ticket ticket, Member actor) {
        this.ticket = ticket;
        this.actor = actor;
    }
}