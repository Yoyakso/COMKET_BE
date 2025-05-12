package com.yoyakso.comket.ticket.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum TicketState {
	TODO(1),
	IN_PROGRESS(2),
	DONE(3),
	HOLD(4),
	DROP(5),
	BACKLOG(6),
	DELETED(7);

	private final int stateValue;

	TicketState(int stateValue) {
		this.stateValue = stateValue;
	}

	@JsonValue
	public String toJson() {
		return name();
	}
}
