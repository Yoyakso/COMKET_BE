package com.yoyakso.comket.ticket.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.ticket.enums.TicketType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketTypeUpdateRequest {
	@NotNull
	@JsonProperty("ticket_ids")
	private List<Long> ticketIds;

	@NotNull
	@JsonProperty("ticket_type")
	private TicketType type;
}
