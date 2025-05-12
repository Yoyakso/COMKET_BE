package com.yoyakso.comket.ticket.enums;

import lombok.Getter;

@Getter
public enum TicketPriority {
	LOW(1),
	MEDIUM(2),
	HIGH(3),
	URGENT(4);

	private final int priorityValue;

	TicketPriority(int priorityValue) {
		this.priorityValue = priorityValue;
	}

}
