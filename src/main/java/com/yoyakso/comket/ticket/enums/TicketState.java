package com.yoyakso.comket.ticket.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum TicketState {
	OPEN(1),
	IN_PROGRESS(2),
	CLOSED(3),
	DELETED(4);

	private final int stateValue;

	TicketState(int stateValue) {
		this.stateValue = stateValue;
	}

	@JsonValue
	public String toJson() {
		return name();
	}
}
