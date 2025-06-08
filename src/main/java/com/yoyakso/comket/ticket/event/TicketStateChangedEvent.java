package com.yoyakso.comket.ticket.event;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.enums.TicketState;

import lombok.Getter;

/**
 * Event that is published when a ticket's state is changed.
 */
@Getter
public class TicketStateChangedEvent extends TicketEvent {
    private final TicketState newState;

    public TicketStateChangedEvent(Ticket ticket, Member actor, TicketState newState) {
        super(ticket, actor);
        this.newState = newState;
    }

    /**
     * Get the new state as a string
     */
    public String getNewStateAsString() {
        return newState != null ? newState.toString() : null;
    }
}
